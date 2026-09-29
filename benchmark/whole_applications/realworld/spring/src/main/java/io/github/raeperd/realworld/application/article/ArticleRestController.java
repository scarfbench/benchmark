package io.github.raeperd.realworld.application.article;

import io.github.raeperd.realworld.domain.article.ArticleService;
import io.github.raeperd.realworld.domain.user.UserName;
import io.github.raeperd.realworld.infrastructure.jwt.UserJWTPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.of;

@RestController
class ArticleRestController {

    private final ArticleService articleService;

    ArticleRestController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/articles")
    public ArticleModel postArticle(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                    @Valid @RequestBody ArticlePostRequestDTO dto) {
        var articleCreated = articleService.createNewArticle(jwtPayload.getUserId(), dto.toArticleContents());
        return ArticleModel.fromArticle(articleCreated);
    }

    @GetMapping("/articles")
    public MultipleArticleModel getArticles(@RequestParam(defaultValue = "0") int offset,
                                            @RequestParam(defaultValue = "20") int limit) {
        final var articles = articleService.getArticles(new OffsetPageRequest(offset, limit));
        return MultipleArticleModel.fromArticles(articles);
    }

    @GetMapping(value = "/articles", params = {"author"})
    public MultipleArticleModel getArticlesByAuthor(@RequestParam String author,
                                                    @RequestParam(defaultValue = "0") int offset,
                                                    @RequestParam(defaultValue = "20") int limit) {
        final var articles = articleService.getArticlesByAuthorName(author, new OffsetPageRequest(offset, limit));
        return MultipleArticleModel.fromArticles(articles);
    }

    @GetMapping(value = "/articles", params = {"tag"})
    public MultipleArticleModel getArticlesByTag(@RequestParam String tag,
                                                 @RequestParam(defaultValue = "0") int offset,
                                                 @RequestParam(defaultValue = "20") int limit) {
        final var articles = articleService.getArticlesByTag(tag, new OffsetPageRequest(offset, limit));
        return MultipleArticleModel.fromArticles(articles);
    }

    @GetMapping(value = "/articles", params = {"favorited"})
    public MultipleArticleModel getArticleByFavoritedUsername(@RequestParam UserName favorited,
                                                              @RequestParam(defaultValue = "0") int offset,
                                                              @RequestParam(defaultValue = "20") int limit) {
        final var articles = articleService.getArticleFavoritedByUsername(favorited, new OffsetPageRequest(offset, limit));
        return MultipleArticleModel.fromArticles(articles);
    }

    @GetMapping("/articles/feed")
    public MultipleArticleModel getFeed(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                        @RequestParam(defaultValue = "0") int offset,
                                        @RequestParam(defaultValue = "20") int limit) {
        final var articles = articleService.getFeedByUserId(jwtPayload.getUserId(), new OffsetPageRequest(offset, limit));
        return MultipleArticleModel.fromArticles(articles);
    }

    @GetMapping("/articles/{slug}")
    public ResponseEntity<ArticleModel> getArticleBySlug(@PathVariable String slug) {
        return of(articleService.getArticleBySlug(slug)
                .map(ArticleModel::fromArticle));
    }

    @PutMapping("/articles/{slug}")
    public ArticleModel putArticleBySlug(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                         @PathVariable String slug,
                                         @RequestBody ArticlePutRequestDTO dto) {
        final var articleUpdated = articleService.updateArticle(jwtPayload.getUserId(), slug, dto.toUpdateRequest());
        return ArticleModel.fromArticle(articleUpdated);
    }

    @PostMapping("/articles/{slug}/favorite")
    public ArticleModel favoriteArticleBySlug(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                              @PathVariable String slug) {
        var articleFavorited = articleService.favoriteArticle(jwtPayload.getUserId(), slug);
        return ArticleModel.fromArticle(articleFavorited);
    }

    @DeleteMapping("/articles/{slug}/favorite")
    public ArticleModel unfavoriteArticleBySlug(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                                @PathVariable String slug) {
        var articleUnfavored = articleService.unfavoriteArticle(jwtPayload.getUserId(), slug);
        return ArticleModel.fromArticle(articleUnfavored);
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/articles/{slug}")
    public void deleteArticleBySlug(@AuthenticationPrincipal UserJWTPayload jwtPayload,
                                    @PathVariable String slug) {
        articleService.deleteArticleBySlug(jwtPayload.getUserId(), slug);
    }
}
