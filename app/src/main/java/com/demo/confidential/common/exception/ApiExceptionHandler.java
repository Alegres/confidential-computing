package com.demo.confidential.common.exception;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Order(HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApiExceptionHandler {

//    @ExceptionHandler(UnknownDeviceException.class)
//    public ResponseEntity<ApiBusinessErrorResponse> toResponse(final HttpServletRequest request,
//                                                               final UnknownDeviceException unknownDeviceException) {
//        return unknownDeviceException.toResponse(request);
//    }
}

