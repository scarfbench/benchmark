import http from 'k6/http';
import chai, { describe, expect } from 'https://jslib.k6.io/k6chaijs/4.3.4.3/index.js';
import { Httpx, Get } from 'https://jslib.k6.io/httpx/0.0.6/index.js';

chai.config.logFailures = true;

let session = new Httpx({ baseURL: 'http://localhost:8080/api' });
const email = __ENV.EMAIL || 'test@example.com';
const password = __ENV.PASSWORD || 'password';
const username = __ENV.USERNAME || 'testuser';
const slug = 'how-to-train-your-dragon';
var commentId;

export let options = {
  thresholds: {
    // fail the test if any checks fail or any requests fail
    checks: ['rate == 1.00'],
    http_req_failed: ['rate == 0.00'],
  },
  vus: 1,
  iterations: 1,
};

export let params = {
  headers: { 'Content-Type': 'application/json' },
};

function testAuth() {
  describe('Register', () => {
    let user = {
      username: username,
      password: password,
      email: email,
    };

    let response = session.post(`/users`, JSON.stringify({ 'user': user }), params);

    expect(response.status, 'registration status').to.equal(201);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(email)
    expect(response.json('user.username'), 'username').to.equal(username)
    expect(response.json('user.bio'), 'bio').to.be.null;
    expect(response.json('user.image'), 'image').to.be.null;
    expect(response.json('user.token'), 'token').to.not.be.null;
  });
  describe('Login', () => {
    let user = {
      password: password,
      email: email,
    };

    let response = session.post(`/users/login`, JSON.stringify({ 'user': user }), params);

    expect(response.status, 'login status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(email)
    expect(response.json('user.username'), 'username').to.equal(username)
    expect(response.json('user.bio'), 'bio').to.be.null;
    expect(response.json('user.image'), 'image').to.be.null;
    expect(response.json('user.token'), 'token').to.not.be.null;
  });
  describe('Login and remember token', () => {
    let user = {
      password: password,
      email: email,
    };

    let response = session.post(`/users/login`, JSON.stringify({ 'user': user }), params);

    expect(response.status, 'login status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(email)
    expect(response.json('user.username'), 'username').to.equal(username)
    expect(response.json('user.bio'), 'bio').to.be.null;
    expect(response.json('user.image'), 'image').to.be.null;
    let authToken = response.json('user.token')
    expect(authToken, 'token').to.not.be.null;
    // set the authorization header on the session for the subsequent requests.
    session.addHeader('Authorization', `Token ${authToken}`)
  });
  describe('Current User', () => {

    let response = session.get(`/user`);

    expect(response.status, 'user status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(email)
    expect(response.json('user.username'), 'username').to.equal(username)
    expect(response.json('user.bio'), 'bio').to.be.null;
    expect(response.json('user.image'), 'image').to.be.null;
    let authToken = response.json('user.token');
    expect(authToken, 'token').to.not.be.null;
  });
  describe('Update User', () => {
    let user = {
      bio: `my-new-bio`,
    };

    let response = session.put(`/user`, JSON.stringify({ 'user': user }), params);

    expect(response.status, 'user status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(email)
    expect(response.json('user.username'), 'username').to.equal(username)
    expect(response.json('user.bio'), 'bio').to.equal('my-new-bio');
    expect(response.json('user.image'), 'image').to.be.null;
    let authToken = response.json('user.token');
    expect(authToken, 'token').to.not.be.null;
  });
}
function testArticles() {
  describe('All articles', () => {
    let response = session.get(`/articles`, params);

    expect(response.status, 'get all articles status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
  describe('Articles by Author', () => {
    let response = session.get(`/articles?author=johnjacob`, params);

    expect(response.status, 'registration status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
  describe('Articles Favorited by Username', () => {
    let response = session.get(`/articles?favorited=jane`, params);

    expect(response.status, 'registration status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
  describe('Articles by Tag', () => {
    let response = session.get(`/articles?tag=dragons`, params);

    expect(response.status, 'registration status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
}
function testArticlesFavoriteComments() {
  describe('Create articles', () => {
    let article = {
      title: `How to train your dragon`,
      description: `Ever wonder how?`,
      body: `Very carefully.`,
      tagList: ["dragons", "training"],
    };

    let response = session.post(`/articles`, JSON.stringify({ 'article': article }), params);
    expect(response.status, 'create status').to.equal(201);
    expect(response).to.have.validJsonBody();
    expect(response.json('article'), 'article').to.be.an('object');
    expect(response.json('article'), 'article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('article.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('article.slug'), 'slug').to.equal(slug)
    expect(response.json('article.body'), 'body').to.equal('Very carefully.');
    expect(response.json('article.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('article.tagList'), 'tagList').to.be.an('array')
    expect(response.json('article.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Feed', () => {
    let response = session.get(`/articles/feed`);

    expect(response.status, 'Feed status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
  describe('All articles after creation', () => {
    let response = session.get(`/articles`, params);

    expect(response.status, 'get all articles status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(1);
    expect(response.json('articles.0'), 'First article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('articles.0.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('articles.0.slug'), 'slug').to.equal(slug)
    expect(response.json('articles.0.body'), 'body').to.equal('Very carefully.');
    expect(response.json('articles.0.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('articles.0.tagList'), 'tagList').to.be.an('array')
    expect(response.json('articles.0.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Articles by Author after creation', () => {
    let response = session.get(`/articles?author=${username}`, params);

    expect(response.status, 'get articles by author status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(1);
    expect(response.json('articles.0'), 'First article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('articles.0.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('articles.0.slug'), 'slug').to.equal(slug)
    expect(response.json('articles.0.body'), 'body').to.equal('Very carefully.');
    expect(response.json('articles.0.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('articles.0.tagList'), 'tagList').to.be.an('array')
    expect(response.json('articles.0.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Articles Favorited by Username after creation', () => {
    let response = session.get(`/articles?favorited=jane`, params);

    expect(response.status, 'get articles favorited by username status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(0);
  });
  describe('Single Article by slug after creation', () => {
    let response = session.get(`/articles/${slug}`, params);

    expect(response.status, 'get articles by slug status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('article'), 'article').to.be.an('object');
    expect(response.json('article'), 'article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('article.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('article.slug'), 'slug').to.equal(slug)
    expect(response.json('article.body'), 'body').to.equal('Very carefully.');
    expect(response.json('article.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('article.tagList'), 'tagList').to.be.an('array')
    expect(response.json('article.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Articles by tag after creation', () => {
    let response = session.get(`/articles?tag=dragons`, params);

    expect(response.status, 'get single article status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('articles'), 'articles').to.be.an('array');
    expect(response.json('articlesCount'), 'articlesCount').to.be.a('number');
    expect(response.json('articlesCount'), 'articlesCount').to.equal(1);
    expect(response.json('articles.0'), 'Article').to.be.an('object');
    expect(response.json('articles.0'), 'Article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('articles.0.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('articles.0.slug'), 'slug').to.equal(slug)
    expect(response.json('articles.0.body'), 'body').to.equal('Very carefully.');
    expect(response.json('articles.0.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('articles.0.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('articles.0.tagList'), 'tagList').to.be.an('array')
    expect(response.json('articles.0.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('articles.0.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Update article', () => {
    let article = {
      body: `With two hands`,
    };

    let response = session.put(`/articles/${slug}`, JSON.stringify({ 'article': article }), params);

    expect(response.status, 'update article status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('article'), 'article').to.be.an('object');
    expect(response.json('article'), 'article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('article.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('article.slug'), 'slug').to.equal(slug)
    expect(response.json('article.body'), 'body').to.equal('With two hands');
    expect(response.json('article.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('article.tagList'), 'tagList').to.be.an('array')
    expect(response.json('article.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Favorite article', () => {

    let response = session.post(`/articles/${slug}/favorite`, params);

    expect(response.status, 'update article status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('article'), 'article').to.be.an('object');
    expect(response.json('article'), 'article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('article.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('article.slug'), 'slug').to.equal(slug)
    expect(response.json('article.body'), 'body').to.equal('With two hands');
    expect(response.json('article.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('article.tagList'), 'tagList').to.be.an('array')
    expect(response.json('article.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('article.favorited'), 'favorited').to.be.true
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.equal(1)
  });
  describe('Unfavorite article', () => {

    let response = session.delete(`/articles/${slug}/favorite`, params);

    expect(response.status, 'update article status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('article'), 'article').to.be.an('object');
    expect(response.json('article'), 'article').to.include.keys('title', 'slug', 'body', 'createdAt', 'updatedAt', 'description', 'tagList', 'author', 'favorited', 'favoritesCount');
    expect(response.json('article.title'), 'title').to.equal('How to train your dragon')
    expect(response.json('article.slug'), 'slug').to.equal(slug)
    expect(response.json('article.body'), 'body').to.equal('With two hands');
    expect(response.json('article.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('article.description'), 'description').to.equal('Ever wonder how?')
    expect(response.json('article.tagList'), 'tagList').to.be.an('array')
    expect(response.json('article.tagList'), 'tagList').to.have.members(['training', 'dragons'])
    expect(response.json('article.favorited'), 'favorited').to.be.false
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.be.a('number')
    expect(response.json('article.favoritesCount'), 'favoritesCount').to.equal(0)
  });
  describe('Create Comment for Article', () => {

    let comment = {
      body: `Thank you so much!`,
    };

    let response = session.post(`/articles/${slug}/comments`, JSON.stringify({ 'comment': comment }), params);

    expect(response.status, 'create comment status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('comment'), 'comment').to.be.an('object');
    expect(response.json('comment'), 'comment').to.include.keys('id', 'body', 'createdAt', 'updatedAt', 'author');
    commentId = response.json('comment.id')
    expect(response.json('comment.body'), 'body').to.be.a('string')
    expect(response.json('comment.body'), 'body').to.equal('Thank you so much!')
    expect(response.json('comment.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('comment.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
  });
  describe('Get Comments for Article', () => {

    let response = session.get(`/articles/${slug}/comments`, params);

    expect(response.status, 'get comment status').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('comments'), 'comments').to.be.an('array');
    expect(response.json('comments.0'), 'first comment').to.be.an('object');
    expect(response.json('comments.0'), 'comment').to.include.keys('id', 'body', 'createdAt', 'updatedAt', 'author');
    expect(response.json('comments.0.body'), 'body').to.be.a('string')
    expect(response.json('comments.0.body'), 'body').to.equal('Thank you so much!')
    expect(response.json('comments.0.createdAt'), 'created at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
    expect(response.json('comments.0.updatedAt'), 'updated at is an ISO 8601 timestamp').to.match(/^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$/)
  });
  describe('Delete Comment for Article', () => {

    let response = session.delete(`/articles/${slug}/comments/${commentId}`, params);

    expect(response.status, 'delete comment status').to.equal(200);
  });
  describe('Delete Article', () => {
    let response = session.delete(`/articles/${slug}`);

    expect(response.status, 'delete status').to.equal(200);
  });
}
function testProfiles() {
  describe('Register Celeb', () => {
    let user = {
      email: `celeb_${email}`,
      password: password,
      username: `celeb_${username}`,
    };

    let response = session.post(`/users`, JSON.stringify({ 'user': user }), params);
    expect(response.status, 'create celeb').to.equal(201);
    expect(response).to.have.validJsonBody();
    expect(response.json('user'), 'user').to.be.an('object');
    expect(response.json('user'), 'user').to.include.keys('email', 'username', 'bio', 'image', 'token');
    expect(response.json('user.email'), 'email').to.equal(`celeb_${email}`)
    expect(response.json('user.username'), 'username').to.equal(`celeb_${username}`)
    expect(response.json('user.bio'), 'bio').to.be.null;
    expect(response.json('user.image'), 'image').to.be.null;
    expect(response.json('user.token'), 'token').to.not.be.null;
  });
  describe('Get Celeb profile', () => {

    let response = session.get(`/profiles/celeb_${username}`, params);
    expect(response.status, 'get celeb profile').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('profile'), 'profile').to.be.an('object');
    expect(response.json('profile'), 'profile').to.include.keys('username', 'bio', 'image', 'following');
    expect(response.json('profile.username'), 'username').to.equal(`celeb_${username}`)
    expect(response.json('profile.bio'), 'bio').to.be.null;
    expect(response.json('profile.image'), 'image').to.be.null;
    expect(response.json('profile.following'), 'following').to.be.false;
  });
  describe('Follow Celeb profile', () => {

    let response = session.post(`/profiles/celeb_${username}/follow`, params);
    expect(response.status, 'follow celeb profile').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('profile'), 'profile').to.be.an('object');
    expect(response.json('profile'), 'profile').to.include.keys('username', 'bio', 'image', 'following');
    expect(response.json('profile.username'), 'username').to.equal(`celeb_${username}`)
    expect(response.json('profile.bio'), 'bio').to.be.null;
    expect(response.json('profile.image'), 'image').to.be.null;
    expect(response.json('profile.following'), 'following').to.be.true;
  });
  describe('Unfollow Celeb profile', () => {

    let response = session.delete(`/profiles/celeb_${username}/follow`, params);
    expect(response.status, 'follow celeb profile').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('profile'), 'profile').to.be.an('object');
    expect(response.json('profile'), 'profile').to.include.keys('username', 'bio', 'image', 'following');
    expect(response.json('profile.username'), 'username').to.equal(`celeb_${username}`)
    expect(response.json('profile.bio'), 'bio').to.be.null;
    expect(response.json('profile.image'), 'image').to.be.null;
    expect(response.json('profile.following'), 'following').to.be.false;
  });
}
function testTags() {
  describe('All Tags', () => {

    let response = session.get(`/tags`, params);
    expect(response.status, 'get all tags').to.equal(200);
    expect(response).to.have.validJsonBody();
    expect(response.json('tags'), 'tags').to.be.an('array');
    expect(response.json('tags'), 'tags').to.have.members(['training', 'dragons'])
  });
}


export default function testSuite() {
  testAuth();
  testArticles();
  testArticlesFavoriteComments();
  testProfiles();
  testTags();
}