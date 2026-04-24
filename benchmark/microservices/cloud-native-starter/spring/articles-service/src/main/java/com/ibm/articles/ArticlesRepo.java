package com.ibm.articles;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.CrudRepository;

public interface ArticlesRepo extends PagingAndSortingRepository<Article, Integer>, CrudRepository<Article, Integer> {
}
