package com.opuscapita.peppol.processor.consumer;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.PeppolMessageMetadata;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Route;
import com.opuscapita.peppol.commons.eventing.EventReporter;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import com.opuscapita.peppol.commons.queue.consume.ContainerMessageConsumer;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.processor.router.ContainerMessageRouter;
import com.opuscapita.peppol.processor.validator.ContainerMetadataValidator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessorMessageConsumer implements ContainerMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorMessageConsumer.class);

    private Storage storage;
    private MessageQueue messageQueue;
    private EventReporter eventReporter;
    private TicketReporter ticketReporter;
    private ContainerMessageRouter messageRouter;
    private ContainerMetadataValidator metadataValidator;

    @Autowired
    public ProcessorMessageConsumer(Storage storage, MessageQueue messageQueue,
                                    EventReporter eventReporter, TicketReporter ticketReporter,
                                    ContainerMessageRouter messageRouter, ContainerMetadataValidator metadataValidator) {
        this.storage = storage;
        this.messageQueue = messageQueue;
        this.eventReporter = eventReporter;
        this.messageRouter = messageRouter;
        this.ticketReporter = ticketReporter;
        this.metadataValidator = metadataValidator;
    }

    @Override
    public void consume(@NotNull ContainerMessage cm) throws Exception {
        cm.setStep(ProcessStep.PREPROCESS);
        cm.getHistory().addInfo("Received and started processing");
        logger.info("Processor received the message: " + cm.toKibana());

        if (StringUtils.isBlank(cm.getFileName())) {
            throw new IllegalArgumentException("File name is empty in received message: " + cm.toKibana());
        }

        logger.info("Checking metadata of the message: " + cm.getFileName());
        metadataValidator.validate(cm);
        if (cm.getHistory().hasError()) {
            logger.info("Processing failed for the message: " + cm.toKibana() + " reason: " + cm.getHistory().getLastLog().getMessage());
            cm.getHistory().addInfo("Processing failed: invalid metadata");

            eventReporter.reportStatus(cm);
            ticketReporter.reportWithContainerMessage(cm, null, "Processing failed for the message: " + cm.getFileName());
            return;
        }

        logger.info("Moving message: " + cm.getFileName() + " to long-term storage");
        PeppolMessageMetadata metadata = cm.getMetadata();
        String path = storage.moveToPermanent(cm.getFileName(), metadata.getSenderId(), metadata.getRecipientId());
        cm.getHistory().addInfo("Moved to long-term storage");
        cm.setFileName(path);

        logger.info("Loading route info for the message: " + cm.getFileName());
        Route route = messageRouter.loadRoute(cm);
        cm.getHistory().addInfo("Route info loaded");
        cm.setRoute(route);

        if (cm.getHistory().hasError()) {
            logger.info("Processing failed for the message: " + cm.toKibana() + " reason: " + cm.getHistory().getLastLog().getMessage());
            cm.getHistory().addInfo("Processing failed: invalid route");

            eventReporter.reportStatus(cm);
            ticketReporter.reportWithContainerMessage(cm, null, "Processing failed for the message: " + cm.getFileName());
            return;
        }

        String outputQueue = cm.popRoute();
        messageQueue.convertAndSend(outputQueue, cm);
        cm.getHistory().addInfo("Processing completed successfully");
        logger.info("The message: " + cm.toKibana() + " successfully processed and delivered to " + outputQueue + " queue");
    }

}
