CREATE INDEX USR_index
on USR(userId);


CREATE INDEX WORK_EXPR_index
on WORK_EXPR(userId,company,role,startDate);


CREATE INDEX EDUCATIONAL_DETAILS_index
on EDUCATIONAL_DETAILS(userId,major,degree);


CREATE INDEX MESSAGE_index
on MESSAGE(msgId);


CREATE INDEX CONNECTION_USR_index
on CONNECTION_USR(userId,connectionId);