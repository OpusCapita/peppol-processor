package com.opuscapita.peppol.processor.validator;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.metadata.PeppolMessageMetadata;
import com.opuscapita.peppol.commons.storage.Storage;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ContainerMetadataValidator {

    private static final Logger logger = LoggerFactory.getLogger(ContainerMetadataValidator.class);

    private Storage storage;
    private MetadataExtractor metadataExtractor;

    @Autowired
    public ContainerMetadataValidator(Storage storage, MetadataExtractor metadataExtractor) {
        this.storage = storage;
        this.metadataExtractor = metadataExtractor;
    }

    public void validate(@NotNull ContainerMessage cm) {
        PeppolMessageMetadata metadata = cm.getMetadata();

        if (metadata == null) {
            logger.info("No metadata info found in the message: " + cm.getFileName());
            try {
                metadata = extractMetadata(cm);
            } catch (Exception e) {
                logger.error("Could not extract the metadata from file: " + cm.getFileName(), e);
                cm.getHistory().addError(e.getMessage());
                return;
            }
        }

        List<String> missingFields = new ArrayList<>();
        if (StringUtils.isBlank(metadata.getSenderId())) {
            missingFields.add("senderId");
        }
        if (StringUtils.isBlank(metadata.getRecipientId())) {
            missingFields.add("receiverId");
        }
        if (StringUtils.isBlank(metadata.getDocumentTypeIdentifier())) {
            missingFields.add("documentTypeIdentifier");
        }
        if (StringUtils.isBlank(metadata.getProfileTypeIdentifier())) {
            missingFields.add("profileTypeIdentifier");
        }

        if (!missingFields.isEmpty()) {
            String tmp = missingFields.stream().collect(Collectors.joining(", "));
            cm.getHistory().addError("Missing some metadata information [" + tmp + "] for the file: " + cm.getFileName());
        }
    }

    private PeppolMessageMetadata extractMetadata(@NotNull ContainerMessage cm) throws Exception {
        logger.debug("Processor will try to extract it from the payload");
        InputStream content = storage.get(cm.getFileName());

        PeppolMessageMetadata metadata = metadataExtractor.extract(content);
        String ocApCommonName = PeppolMessageMetadata.OC_AP_COMMON_NAME;
        if (cm.isInbound()) {
            metadata.setReceivingAccessPoint(ocApCommonName);
        } else {
            metadata.setSendingAccessPoint(ocApCommonName);
        }

        cm.setMetadata(metadata);
        return metadata;
    }

}
