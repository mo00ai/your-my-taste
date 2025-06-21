package com.example.taste.domain.pk.repository;

import java.util.List;

import com.example.taste.domain.pk.entity.PkLog;

public interface PkLogRepositoryJooqCustom {
	void insertPkLogs(List<PkLog> pkLogs);

}
