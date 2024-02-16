import 'dart:async';
import 'dart:convert';
import 'dart:developer';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:dio/io.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:frontend/domain/auth/auth_constants.dart';
import 'package:frontend/domain/auth/token.dart';
import 'package:frontend/util/pair.dart';

class AuthenticationManager {
  final String _tokenEndPoint;
  final String _client;
  final String _secret;

  final Function(bool) _isAuthenticatedCallback;

  final Dio _refreshDio = Dio();
  final Dio client = Dio();

  AuthenticationManager(this._tokenEndPoint, this._client, this._secret, this._isAuthenticatedCallback) {
    if (!kIsWeb && kDebugMode) {
      _refreshDio.httpClientAdapter = IOHttpClientAdapter(createHttpClient: () {
        final client = HttpClient();
        client.findProxy = (uri) {
          return 'PROXY localhost:8888';
        };
        return client;
      });
      client.httpClientAdapter = IOHttpClientAdapter(createHttpClient: () {
        final client = HttpClient();
        client.findProxy = (uri) {
          return 'PROXY localhost:8888';
        };
        return client;
      });
    }

    _configure();
  }

  final _storage = const FlutterSecureStorage();

  Token? _token;

  void _configure() {
    client.interceptors.clear();

    client.interceptors
        .add(QueuedInterceptorsWrapper(onRequest: (RequestOptions options, RequestInterceptorHandler handler) async {
      // In memory accessToken
      final mToken = _token;

      if (mToken != null && !mToken.isExpired) {
        options.headers[AuthConstants.keyAuthorization] = "${AuthConstants.keyBearer} ${mToken.accessToken}";
        return handler.next(options);
      }

      // Stored refreshToken
      try {
        final Token? mRefreshedToken = await tryRefresh();

        if (mRefreshedToken != null) {
          options.headers[AuthConstants.keyAuthorization] = "${AuthConstants.keyBearer} ${mRefreshedToken.accessToken}";
          return handler.next(options);
        }
      } catch (e) {
        _isAuthenticatedCallback(false);
        // just continue execution
        if (e is DioException) {
          handler.reject(e);
        } else {
          return handler.reject(DioException(requestOptions: options, error: e));
        }
      }
    }, onError: (final DioException error, ErrorInterceptorHandler handler) async {
      if (error.response?.statusCode == 401) {
        _token = null;
        // Stored refreshToken
        try {
          final Token? mRefreshedToken = await tryRefresh();

          if (mRefreshedToken != null) {
            error.requestOptions.headers[AuthConstants.keyAuthorization] =
                "${AuthConstants.keyBearer} ${mRefreshedToken.accessToken}";

            var retry = await _refreshDio.fetch(error.requestOptions);
            return handler.resolve(retry);
          }
        } catch (e) {
          log('Refresh token error', error: e);
          // just continue execution
          // TODO authChangeNotifier.setLoggedIn(false);

          _isAuthenticatedCallback(false);
          if (e is DioException) {
            return handler.reject(e);
          } else {
            return handler.reject(DioException(requestOptions: RequestOptions(), error: e));
          }
        }
      }
      log('GO TO NEXT');
      return handler.next(error);
    }, onResponse: (Response response, ResponseInterceptorHandler handler) {
      return handler.next(response);
    }));
  }

  Future<void> authenticate(Pair<String, String> credentials) async {
    Token? token;

    final Response tokenResponse = await _refreshDio.post(_tokenEndPoint,
        data: {
          AuthConstants.keyGrantType: AuthConstants.keyPassword,
          AuthConstants.keyUserName: credentials.first,
          AuthConstants.keyPassword: credentials.second,
          AuthConstants.keyScope: "tenant_admin offline_access"
        },
        options: Options(headers: {
          AuthConstants.keyAuthorization: "${AuthConstants.keyBasic} ${base64Encode(utf8.encode("$_client:$_secret"))}"
        }, contentType: Headers.formUrlEncodedContentType));

    token = Token.fromJson(tokenResponse.data);

    _isAuthenticatedCallback(true);

    if (token.refreshToken != null) {
      _writeRefreshToken(token.refreshToken!);
    }

    _token = token;
  }

  Future<Token?> tryRefresh() async {
    final String? storedRefreshToken = await _readRefreshToken();

    if (storedRefreshToken != null) {
      Token token;
      try {
        Response tokenResponse = await _refreshDio.post(_tokenEndPoint,
            data: {
              AuthConstants.keyGrantType: AuthConstants.keyRefreshToken,
              AuthConstants.keyRefreshToken: storedRefreshToken
            },
            options: Options(headers: {
              AuthConstants.keyAuthorization:
                  "${AuthConstants.keyBasic} ${base64Encode(utf8.encode("$_client:$_secret"))}"
            }, contentType: Headers.formUrlEncodedContentType));

        token = Token.fromJson(tokenResponse.data);

        final String? mRefreshToken = token.refreshToken;
        if (mRefreshToken != null) {
          _writeRefreshToken(mRefreshToken);
        }

        _token = token;

        _isAuthenticatedCallback(true);

        return token;
      } catch (e) {
        if (e is DioException && e.response?.statusCode == 400) {
          // clear refresh token
          _clearRefreshToken();
        }
        rethrow;
      }
    }
    _isAuthenticatedCallback(false);
    return null;
  }

  Future<void> logout() async {
    await _clearRefreshToken();
    _token = null;
    _isAuthenticatedCallback(false);
  }

  Future<String?> _readRefreshToken() async {
    return await _storage.read(key: AuthConstants.keyRefreshTokenStoreKey);
  }

  void _writeRefreshToken(String refreshToken) {
    _storage.write(key: AuthConstants.keyRefreshTokenStoreKey, value: refreshToken);
  }

  Future<void> _clearRefreshToken() async {
    await _storage.delete(key: AuthConstants.keyRefreshTokenStoreKey);
  }
}
