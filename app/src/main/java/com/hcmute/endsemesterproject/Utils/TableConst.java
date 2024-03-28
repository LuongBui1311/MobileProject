package com.hcmute.endsemesterproject.Utils;

public class TableConst {

    public class USERS {
        public static final String TABLE_NAME = "Users";
        public static final String DEVICE_TOKEN = "device_token";
        public static final String UID = "uid";
        public static final String IMAGE = "image";
        public static final String NAME = "name";
        public static final String STATUS = "status";

        public class USER_STATE{
            public static final String DATE = "date";
            public static final String STATE = "state";
            public static final String TIME = "time";
        }
    }

    public class CHAT_REQUESTS {
        public static final String TABLE_NAME = "Chat Requests";
        public static final String REQUEST_TYPE = "request_type";
        public static final String REQUEST_TYPE_SENT = "sent";
        public static final String REQUEST_TYPE_RECEIVED = "received";
    }

    public class CONTACTS {
        public static final String TABLE_NAME = "Contacts";
        public static final String CONTACT = "Contact";
        public static final String CONTACT_SAVED = "Saved";
    }

    public class GROUPS {
        public static final String TABLE_NAME = "Groups";
        public static final String DATE = "date";
        public static final String MESSAGE = "message";
        public static final String NAME = "name";
        public static final String TIME = "time";
    }

    public class MESSAGES {
        public static final String TABLE_NAME = "Messages";
        public static final String DATE = "date";
        public static final String FROM = "from";
        public static final String MESSAGE = "message";
        public static final String MESSAGE_ID = "messageID";
        public static final String TO = "to";
        public static final String TIME = "time";

        public class TYPE{
            public static final String TABLE_NAME = "type";
            public static final String TYPE_TEXT = "text";
            public static final String TYPE_IMAGE = "image";
            public static final String TYPE_PDF = "pdf";
            public static final String TYPE_DOCX = "docx";
        }
    }

    public class NOTIFICATIONS {
        public static final String TABLE_NAME = "Notifications";
        public static final String FROM = "from";
        public static final String TYPE = "type";
    }

}
