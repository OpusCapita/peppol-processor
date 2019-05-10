package com.opuscapita.peppol.processor.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataValidator;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Route;
import com.opuscapita.peppol.commons.eventing.EventReporter;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.commons.storage.StorageUtils;
import com.opuscapita.peppol.processor.router.ContainerMessageRouter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class ProcessorMessageConsumer implements ContainerMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorMessageConsumer.class);

    @Value("${peppol.processor.queue.out.name}")
    private String queueOut;

    @Value("${peppol.storage.blob.cold:cold}")
    private String coldFolder;

    private Storage storage;
    private MessageQueue messageQueue;
    private EventReporter eventReporter;
    private TicketReporter ticketReporter;
    private MetadataValidator metadataValidator;
    private ContainerMessageRouter messageRouter;

    @Autowired
    public ProcessorMessageConsumer(Storage storage, MessageQueue messageQueue,
                                    EventReporter eventReporter, TicketReporter ticketReporter,
                                    MetadataValidator metadataValidator, ContainerMessageRouter messageRouter) {
        this.storage = storage;
        this.messageQueue = messageQueue;
        this.eventReporter = eventReporter;
        this.messageRouter = messageRouter;
        this.ticketReporter = ticketReporter;
        this.metadataValidator = metadataValidator;
    }

    @Override
    public void consume(@NotNull ContainerMessage cm) throws Exception {
        cm.setStep(ProcessStep.PROCESSOR);
        cm.getHistory().addInfo("Received and started processing");
        logger.info("Processor received the message: " + cm.toKibana());

        if (StringUtils.isBlank(cm.getFileName())) {
            throw new IllegalArgumentException("File name is empty in received message: " + cm.toKibana());
        }

        logger.debug("Checking metadata of the message: " + cm.getFileName());
        metadataValidator.validate(cm);
        if (cm.getHistory().hasError()) {
            String shortDescription = "Processing failed for '" + cm.getFileName() + "' reason: " + cm.getHistory().getLastLog().getMessage();
            logger.info(shortDescription);
            cm.getHistory().addInfo("Processing failed: invalid metadata");

            eventReporter.reportStatus(cm);
            ticketReporter.reportWithContainerMessage(cm, null, shortDescription);
            return;
        }

        moveFileToLongTermStorage(cm);

        logger.debug("Loading route info for the message: " + cm.getFileName());
        Route route = messageRouter.loadRoute(cm);
        if (cm.getHistory().hasError()) {
            String shortDescription = "Processing failed for '" + cm.getFileName() + "' reason: " + cm.getHistory().getLastLog().getMessage();
            logger.info(shortDescription);
            cm.getHistory().addInfo("Processing failed: invalid route");

            eventReporter.reportStatus(cm);
            ticketReporter.reportWithContainerMessage(cm, null, shortDescription);
            return;
        }
        cm.getHistory().addInfo("Route info loaded, destination: " + route.getDestination());
        cm.setRoute(route);

        cm.getHistory().addInfo("Processing completed successfully");
        logger.info("The message: " + cm.getFileName() + " successfully processed and delivered to " + queueOut + " queue");
        eventReporter.reportStatus(cm);
        messageQueue.convertAndSend(queueOut, cm);
    }

    // a workaround for a race-condition issue, sometimes we try to move the file before it actually stored
    // maybe it is better to move this retry logic to peppol-commons
    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(delay = 9000))
    private void moveFileToLongTermStorage(ContainerMessage cm) throws Exception {
        logger.info("Moving message: " + cm.getFileName() + " to long-term storage");

        ContainerMessageMetadata metadata = cm.getMetadata();
        String dest = StorageUtils.createUserPath(coldFolder, FilenameUtils.getName(cm.getFileName()), metadata.getSenderId(), metadata.getRecipientId());
        String path = storage.move(cm.getFileName(), dest);
        cm.getHistory().addInfo("Moved to long-term storage");
        cm.setFileName(path);
    }
}
