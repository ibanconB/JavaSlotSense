package com.slotsense.contract.v1;

import java.util.Map;

public class FeatureEvent {
    public String eventId;
    public String bonusName;
    public Long creditsWon; // null default
    public Map<String, Object> payload; //JSON-serializable

    public FeatureEvent(){}

    public FeatureEvent(String eventId, String bonusName, Long creditsWon, Map<String, Object> payload) {
        this.eventId = eventId;
        this.bonusName = bonusName;
        this.creditsWon = creditsWon;
        this.payload = payload;
    }
}
