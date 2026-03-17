package org.example.realworldapi.application.web.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArticlesResponse {

    private List<ArticleResponse> articles;
    private long articlesCount;

    public ArticlesResponse(List<ArticleResponse> articles, long articlesCount) {
        this.articles = articles;
        this.articlesCount = articlesCount;
    }
}
