package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface RevenueService {
	
	void save(Revenue typeTrx);

	Iterable<Revenue> findByDateAndOperator(Date start, Date end, String operator);

	List<Object[]> sumByDateAndOperator(Date startDate, Date endDate, String operator);

    List<Revenue> curveByDateAndOperator(Date startDate, Date endDate, String operator);
}
