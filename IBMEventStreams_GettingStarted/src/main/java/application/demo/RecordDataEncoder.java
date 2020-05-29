/*
 *
 * Licensed Materials - Property of IBM
 *
 * 5737-H33
 *
 * (C) Copyright IBM Corp. 2019  All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 */

package application.demo;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import application.demo.RecordData;

public class RecordDataEncoder implements Encoder.Text<RecordData> {

	@Override
	public void init(EndpointConfig config) {}

	@Override
	public void destroy() {}

	@Override
	public String encode(RecordData recordData) throws EncodeException {
		return recordData.encode();
	}

}