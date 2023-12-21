package sn.intouch.gu.lonaciapi.ejb.notification.services;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;

@Local
public interface TypeTrxService {
	
	Iterable<TypeTrx> findAll();
	TypeTrx save(TypeTrx typeTrx);
	TypeTrx getByCode(String code);
	TypeTrx delete(TypeTrx typeTrx);
}
