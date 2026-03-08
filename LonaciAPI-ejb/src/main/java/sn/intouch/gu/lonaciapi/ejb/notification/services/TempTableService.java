package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.Revenue;
import sn.intouch.gu.lonaciapi.ejb.notification.entities.TempTable;

import javax.ejb.Local;
import java.util.Date;
import java.util.List;

@Local
public interface TempTableService {

	boolean add(TempTable tempTable);

}
