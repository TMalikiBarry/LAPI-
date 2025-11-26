package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface RevenueService {

	void add(Revenue typeTrx);

	Revenue update(Revenue revenue);

	List<Object[]> sumByDateAndOperator(Date startDate, Date endDate, String operator, String country);

    List<Revenue> curveByDateAndOperator(Date startDate, Date endDate, String operator, String country);

    void updateRevenueRow(Revenue revenueEntity);
}
