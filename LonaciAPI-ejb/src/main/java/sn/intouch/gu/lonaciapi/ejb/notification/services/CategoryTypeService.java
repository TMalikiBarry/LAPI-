package sn.intouch.gu.lonaciapi.ejb.notification.services;

import sn.intouch.gu.lonaciapi.ejb.notification.entities.CategoryType;

import javax.ejb.Local;

@Local
public interface CategoryTypeService {
	
	Iterable<CategoryType> findAll();
	CategoryType save(CategoryType typeTrx);
	CategoryType getByCode(String code);
	CategoryType delete(CategoryType typeTrx);
}
