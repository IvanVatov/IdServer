import 'package:dio/dio.dart';
import 'package:frontend/domain/model/error_response.dart';

extension ParseError on Object {
  String parseMessage() {
    String? message;

    if (this is DioException) {
      var dioError = this as DioException;

      var errorResponse = dioError.response?.data;

      if (errorResponse != null) {
        try {
          message = ErrorResponse.fromJson(errorResponse as Map<String, dynamic>).description;
        } catch (e) {
          message = e.toString();
        }
      }
    } else if (this is Error) {
      message = toString();
    }

    if (message == null || message.isEmpty) {
      message = 'Something went wrong';
    }

    return message;
  }
}
