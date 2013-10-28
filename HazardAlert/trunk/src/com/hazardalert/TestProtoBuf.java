package com.hazardalert;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.CapValidator;
import com.google.publicalerts.cap.Group;

public class TestProtoBuf {
	public TestProtoBuf() {
		try {
			Alert alert = Alert.newBuilder().setXmlns(CapValidator.CAP_LATEST_XMLNS).setIdentifier("43b080713727").setSender("hsas@dhs.gov").setSent("2003-04-02T14:39:01-05:00").setStatus(Alert.Status.ACTUAL).setMsgType(Alert.MsgType.ALERT).setSource("a source").setScope(Alert.Scope.PUBLIC)
					.setRestriction("a restriction").setAddresses(Group.newBuilder().addValue("address 1").addValue("address2").build()).addCode("abcde").addCode("fghij").setNote("a note").setReferences(Group.newBuilder().addValue("reference1").addValue("reference 2").build())
					.setIncidents(Group.newBuilder().addValue("incident1").addValue("incident2").build()).buildPartial();

			byte[] serialized = alert.toByteArray();

			com.google.publicalerts.cap.Alert.Builder builder = com.google.publicalerts.cap.Alert.newBuilder();
			builder.mergeFrom(serialized);
			Alert fromBytes = builder.build();

			if (!fromBytes.getIdentifier().equals(alert.getIdentifier())) {
				throw new RuntimeException("Id's don't match.");
			}

			Log.i("Tests passed.");
		} catch (Exception e) {
			Log.e("Tests failed!");
			throw new RuntimeException(e);
		}
	}

}
