package sn.intouch.gu.lonaciapi.ejb.notification.services;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.TypeTrx;

@Local
public interface TypeTrxService {
	
	public List<TypeTrx> findAll();
	public TypeTrx getTypeTrxById(Long Id);
	public TypeTrx saveTypeTrx(TypeTrx typeTrx);
	public TypeTrx updateTypeTrx(TypeTrx typeTrx);
	public TypeTrx getTypeTrxByCode(String code);
	public List<TypeTrx> filterTypeTrx(String code);
}
