package com.team13.context.trtc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TrtcRecordingCallbackSignVerifierTest {

    @Test
    void verify_matchesTencentDocExample() throws Exception {
        String key = "123654";
        String body = "{\n"
                + "\t\"EventGroupId\":\t2,\n"
                + "\t\"EventType\":\t204,\n"
                + "\t\"CallbackTs\":\t1664209748188,\n"
                + "\t\"EventInfo\":\t{\n"
                + "\t\t\"RoomId\":\t8489,\n"
                + "\t\t\"EventTs\":\t1664209748,\n"
                + "\t\t\"EventMsTs\":\t1664209748180,\n"
                + "\t\t\"UserId\":\t\"user_85034614\",\n"
                + "\t\t\"Reason\":\t0\n"
                + "\t}\n"
                + "}";
        String sign = "kkoFeO3Oh2ZHnjtg8tEAQhtXK16/KI05W3BQff8IvGA=";

        assertTrue(TrtcRecordingCallbackSignVerifier.verify(key, body, sign));
        assertTrue(sign.equals(TrtcRecordingCallbackSignVerifier.sign(key, body)));
    }
}
