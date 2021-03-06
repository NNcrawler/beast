package com.gojek.beast.commiter;

import lombok.Getter;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Instant;
import java.util.Map;

public class OffsetState {
    @Getter
    private final long acknowledgeTimeoutMs;
    private boolean start;
    private Map<TopicPartition, OffsetAndMetadata> lastCommitOffset;
    private Instant lastCommittedTime;

    public OffsetState(long acknowledgeTimeoutMs) {
        this.acknowledgeTimeoutMs = acknowledgeTimeoutMs;
        lastCommittedTime = Instant.now();
    }

    public boolean shouldCloseConsumer(Map<TopicPartition, OffsetAndMetadata> currentOffset) {
        if (!start) {
            return false;
        }
        boolean sameOffset = lastCommitOffset == currentOffset || currentOffset.equals(lastCommitOffset);
        boolean ackTimedOut = (Instant.now().toEpochMilli() - lastCommittedTime.toEpochMilli()) > acknowledgeTimeoutMs;
        boolean neverAcknowledged = lastCommitOffset == null && ackTimedOut;
        return (sameOffset && ackTimedOut) || neverAcknowledged;
    }

    public void resetOffset(Map<TopicPartition, OffsetAndMetadata> acknowledgedOffset) {
        lastCommitOffset = acknowledgedOffset;
        lastCommittedTime = Instant.now();
    }

    public void startTimer() {
        start = true;
    }
}
