package com.babeeta.hudee.service.message;

@SuppressWarnings("javadoc")
public class MessageStringDefs {
	public static final String DB_FIELD_ID = "_id";
	public static final String DB_FIELD_SENDER = "from";
	public static final String DB_FIELD_RECIPIENT = "to";
	public static final String DB_FIELD_DEVICE = "device";
	public static final String DB_FIELD_MESSAGE = "msg";
	public static final String DB_FIELD_STATUS = "status";
	public static final String DB_FIELD_LIFE = "life";
	public static final String DB_FIELD_CREATEAT = "ct";
	public static final String DB_FIELD_MODIFIEDAT = "mt";
	public static final String DB_FIELD_CLASS = "class";
	public static final String DB_FIELD_PARENT = "parent";

	public static final String DB_FIELD_ACKED = "acked";
	public static final String DB_FIELD_TOTAL = "total";
	public static final String DB_FIELD_CONFIRM = "CONFIRM";

	public static final String CLASS_NORMAL = "NORMAL";
	public static final String CLASS_PARENT = "PARENT";
	public static final String CLASS_EVENT = "EVENT";
	public static final String CLASS_REFERENCE = "REFERENCE";
	public static final String CLASS_CONFIRM = "CONFIRM";

	public static final String STATUS_DELIVERING = "DELIVERING";
	public static final String STATUS_ACKED = "ACKED";
	public static final String STATUS_EXPIRED = "EXPIRED";
}
