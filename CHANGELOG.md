# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## `1.1.1-SNAPSHOT`

Release date `UNRELEASED`



## `1.1.0-39`

Release date `2023-03-10`

- (feat api): Add middleware builder functions.
- (feat api): Add `cookies` middleware.

## `1.0.33`

Release date `2023-03-09`

- (chore): Change license to Unlicense.
- (deps): Upgrade undertow "2.3.4.Final", zmap "1.3.26".

## `1.0.28`

Release date `2023-03-26`

- Add `:body-params` in non-POST request.
- (feat api codec breaking) Rename `:param-key-fn` to `:param-name-fn`.
- (docs) Fix typo in params-request-fn docstring.
- (docs bench) Add benchmark data for `util.headers`.

## `1.0.17-beta2`

Release date `2023-01-31`

- Rename `:url-params` to `:path-or-query-params`.

## `1.0.12-beta1`

Release date `2023-01-21`

- Initial implementation.
- Implement `params` middleware.
