package com.example.taste.common.provider;

import java.util.Objects;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.springframework.boot.autoconfigure.jooq.ExceptionTranslatorExecuteListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

public class SpringExceptionTranslationExecuteProvider
	implements ExceptionTranslatorExecuteListener {
	@Override
	public void exception(ExecuteContext context) {
		SQLDialect dialect = context.configuration().dialect();
		SQLExceptionTranslator translator = getSqlExceptionTranslator(dialect);

		DataAccessException dataAccessException = translator.translate(
			"Data access using JOOQ", context.sql(), context.sqlException());
		DataAccessException translation = Objects.requireNonNullElseGet(dataAccessException,
			() -> new UncategorizedSQLException("translation of exception",
				context.sql(), context.sqlException()));
		context.exception(translation);
	}

	private SQLExceptionTranslator getSqlExceptionTranslator(SQLDialect dialect) {
		return dialect != null ? new SQLErrorCodeSQLExceptionTranslator(dialect.name())
			: new SQLStateSQLExceptionTranslator();
	}
}
