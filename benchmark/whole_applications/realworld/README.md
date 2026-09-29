# RealWorld behavior test

`behavior_test.py` is the shared HTTP contract for the Jakarta, Quarkus, and Spring references. It creates uniquely named users and articles, exercises authentication, article editing and discovery, pagination, follows, feed, favorites, comments, and permissions, then deletes its articles.

Each target has an identical copy because the benchmark builds each Docker image from that target's directory. After editing the root test, refresh and check those copies:

```bash
bash sync_behavior_test.sh
bash sync_behavior_test.sh --check
```

The target's `test.sh` runs its packaged copy. Set `APP_ORIGIN` or `BASE_URL` to change the host or port. The test always requests `/api` paths and uses `Authorization: Token <jwt>` so changes to the application API are visible.
