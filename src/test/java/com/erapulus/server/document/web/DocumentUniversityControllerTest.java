package com.erapulus.server.document.web;

import com.erapulus.server.TestUtils;
import com.erapulus.server.document.dto.DocumentRequestDto;
import com.erapulus.server.document.dto.DocumentResponseDto;
import com.erapulus.server.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = {DocumentUniversityRouter.class, DocumentUniversityController.class},
        excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
class DocumentUniversityControllerTest {

    private final static int DOCUMENT_ID_1 = 1;
    private final static int DOCUMENT_ID_2 = 2;
    private final static int UNIVERSITY_ID = 3;

    @Autowired
    ApplicationContext applicationContext;

    @MockBean
    DocumentService documentService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void listDocumentsForUniversity_shouldReturnDocuments() {
        // given
        var documentList = List.of(createDocumentResponseDto(DOCUMENT_ID_1), createDocumentResponseDto(DOCUMENT_ID_2));
        String expectedPayload = """
                [
                   {
                     "id":1,
                     "name":"name",
                     "description":"description",
                     "path":"path",
                     "universityId":3,
                     "facultyId":null,
                     "programId":null,
                     "moduleId":null
                   },
                   {
                     "id":2,
                     "name":"name",
                     "description":"description",
                     "path":"path",
                     "universityId":3,
                     "facultyId":null,
                     "programId":null,
                     "moduleId":null
                   }
                ]""";
        String expectedResponse = TestUtils.createSuccessfulResponse(HttpStatus.OK.value(), expectedPayload);
        when(documentService.listDocuments(UNIVERSITY_ID, null, null, null)).thenReturn(Mono.just(documentList));

        // when-then
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.OK)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void listDocumentsForUniversity_shouldReturnInternalServerErrorWhenUnexpectedErrorThrown() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
        when(documentService.listDocuments(UNIVERSITY_ID, null, null, null)).thenReturn(Mono.error(new RuntimeException()));

        // when-then
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void uploadDocumentForUniversity_shouldReturnDocumentWhenDataCorrect() {
        // given
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new ClassPathResource("example_file.txt")).contentType(MediaType.MULTIPART_FORM_DATA);
        var universityResponseDto = createDocumentResponseDto(DOCUMENT_ID_1);
        String expectedPayload = """
                {
                   "id":1,
                   "name":"name",
                   "description":"description",
                   "path":"path",
                   "universityId":3,
                   "facultyId":null,
                   "programId":null,
                   "moduleId":null
                }""";
        String expectedResponse = TestUtils.createSuccessfulResponse(HttpStatus.OK.value(), expectedPayload);
        when(documentService.createDocument(eq(UNIVERSITY_ID), eq(null), eq(null), eq(null), anyMap()))
                .thenReturn(Mono.just(universityResponseDto));

        // when-then
        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .contentType(MediaType.MULTIPART_FORM_DATA)
                     .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.OK)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void uploadDocumentForUniversity_shouldReturnBadRequestWhenNoBodyFound() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.BAD_REQUEST.value(), "bad.request;not.found.body");
        when(documentService.createDocument(eq(UNIVERSITY_ID), eq(null), eq(null), eq(null), anyMap()))
                .thenThrow(new IllegalArgumentException());

        // when-then
        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .contentType(MediaType.MULTIPART_FORM_DATA)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void uploadDocumentForUniversity_shouldReturnBadRequestWhenWrongBodyFound() {
        // given
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("wrong_field", new ClassPathResource("example_file.txt")).contentType(MediaType.MULTIPART_FORM_DATA);
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.BAD_REQUEST.value(), "bad.request;not.found.body");
        when(documentService.createDocument(eq(UNIVERSITY_ID), eq(null), eq(null), eq(null), anyMap()))
                .thenThrow(new IllegalArgumentException());

        // when-then
        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .contentType(MediaType.MULTIPART_FORM_DATA)
                     .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void uploadDocumentForUniversity_shouldReturnNotFoundWhenNoSuchElementExceptionThrown() {
        // given
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new ClassPathResource("example_file.txt")).contentType(MediaType.MULTIPART_FORM_DATA);
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.NOT_FOUND.value(), "university.not.found");
        when(documentService.createDocument(eq(UNIVERSITY_ID), eq(null), eq(null), eq(null), anyMap()))
                .thenReturn(Mono.error(new NoSuchElementException("university")));

        // when-then
        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .contentType(MediaType.MULTIPART_FORM_DATA)
                     .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void uploadDocumentForUniversity_shouldReturnInternalServerErrorWhenUnexpectedErrorThrown() {
        // given
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", new ClassPathResource("example_file.txt")).contentType(MediaType.MULTIPART_FORM_DATA);
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
        when(documentService.createDocument(eq(UNIVERSITY_ID), eq(null), eq(null), eq(null), anyMap()))
                .thenReturn(Mono.error(new RuntimeException()));

        // when-then
        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document")
                             .build(UNIVERSITY_ID))
                     .contentType(MediaType.MULTIPART_FORM_DATA)
                     .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void getDocumentByIdForUniversity_shouldReturnDocument() {
        // given
        var documentResponseDto = createDocumentResponseDto(DOCUMENT_ID_1);
        String expectedPayload = """
                {
                   "id":1,
                   "name":"name",
                   "description":"description",
                   "path":"path",
                   "universityId":3,
                   "facultyId":null,
                   "programId":null,
                   "moduleId":null
                }""";
        String expectedResponse = TestUtils.createSuccessfulResponse(HttpStatus.OK.value(), expectedPayload);
        when(documentService.getDocumentById(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.just(documentResponseDto));

        // when-then
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.OK)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void getDocumentByIdForUniversity_shouldReturnNotFoundWhenNoSuchElementExceptionThrown() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.NOT_FOUND.value(), "document.not.found");
        when(documentService.getDocumentById(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.error(new NoSuchElementException("document")));

        // when-then
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void getDocumentByIdForUniversity_shouldReturnInternalServerErrorWhenUnexpectedErrorThrown() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
        when(documentService.getDocumentById(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.error(new RuntimeException()));

        // when-then
        webTestClient.get()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void updateDocumentForUniversity_shouldReturnUpdatedDocumentWhenDataCorrect() {
        // given
        var documentRequestDto = createDocumentRequestDto();
        var documentResponseDto = createDocumentResponseDto(DOCUMENT_ID_1);
        String expectedPayload = """
                {
                   "id":1,
                   "name":"name",
                   "description":"description",
                   "path":"path",
                   "universityId":3,
                   "facultyId":null,
                   "programId":null,
                   "moduleId":null
                }""";
        String expectedResponse = TestUtils.createSuccessfulResponse(HttpStatus.OK.value(), expectedPayload);
        when(documentService.updateDocument(any(DocumentRequestDto.class), eq(DOCUMENT_ID_1), eq(UNIVERSITY_ID), eq(null), eq(null), eq(null)))
                .thenReturn(Mono.just(documentResponseDto));

        // when-then
        webTestClient.put()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(TestUtils.parseToJson(documentRequestDto)))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.OK)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void updateDocumentForUniversity_shouldReturnBadRequestWhenMissingField() {
        // given
        var documentRequestDto = createDocumentRequestDto().name(null);
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.BAD_REQUEST.value(), "bad.request;name.must.not.be.null");
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        when(documentService.updateDocument(any(DocumentRequestDto.class), eq(DOCUMENT_ID_1), eq(UNIVERSITY_ID), eq(null), eq(null), eq(null)))
                .thenThrow(new ConstraintViolationException(validator.validate(documentRequestDto)));

        // when-then
        webTestClient.put()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(TestUtils.parseToJson(documentRequestDto)))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void updateDocumentForUniversity_shouldReturnBadRequestWhenNoBodyFound() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.BAD_REQUEST.value(), "bad.request;not.found.body");
        when(documentService.updateDocument(any(DocumentRequestDto.class), eq(DOCUMENT_ID_1), eq(UNIVERSITY_ID), eq(null), eq(null), eq(null)))
                .thenThrow(new IllegalStateException());

        // when-then
        webTestClient.put()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .contentType(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void updateDocumentForUniversity_shouldReturnNotFoundWhenNoSuchElementExceptionThrown() {
        // given
        var documentRequestDto = createDocumentRequestDto();
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.NOT_FOUND.value(), "document.not.found");
        when(documentService.updateDocument(any(DocumentRequestDto.class), eq(DOCUMENT_ID_1), eq(UNIVERSITY_ID), eq(null), eq(null), eq(null)))
                .thenReturn(Mono.error(new NoSuchElementException("document")));

        // when-then
        webTestClient.put()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(TestUtils.parseToJson(documentRequestDto)))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void updateDocumentForUniversity_shouldReturnInternalServerErrorWhenUnexpectedErrorThrown() {
        // given
        var documentRequestDto = createDocumentRequestDto();
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
        when(documentService.updateDocument(any(DocumentRequestDto.class), eq(DOCUMENT_ID_1), eq(UNIVERSITY_ID), eq(null), eq(null), eq(null)))
                .thenThrow(new RuntimeException());

        // when-then
        webTestClient.put()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(TestUtils.parseToJson(documentRequestDto)))
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void deleteDocumentForUniversity_shouldReturnNoContent() {
        // given
        String expectedPayload = null;
        String expectedResponse = TestUtils.createSuccessfulResponse(HttpStatus.NO_CONTENT.value(), expectedPayload);
        when(documentService.deleteDocument(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.just(true));

        // when-then
        webTestClient.delete()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void deleteDocumentForUniversity_shouldReturnNotFoundWhenNoSuchElementExceptionThrown() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.NOT_FOUND.value(), "document.not.found");
        when(documentService.deleteDocument(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.error(new NoSuchElementException("document")));

        // when-then
        webTestClient.delete()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    @Test
    void deleteDocumentForUniversity_shouldReturnInternalServerErrorWhenUnexpectedErrorThrown() {
        // given
        String expectedResponse = TestUtils.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal.server.error");
        when(documentService.deleteDocument(DOCUMENT_ID_1, UNIVERSITY_ID, null, null, null))
                .thenReturn(Mono.error(new RuntimeException()));

        // when-then
        webTestClient.delete()
                     .uri(uriBuilder -> uriBuilder
                             .path("/api/university/{universityId}/document/{documentId}")
                             .build(UNIVERSITY_ID, DOCUMENT_ID_1))
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody().consumeWith(body -> TestUtils.assertJsonEquals(expectedResponse, TestUtils.getBodyAsString(body)));
    }

    private DocumentResponseDto createDocumentResponseDto(int id) {
        return DocumentResponseDto.builder()
                                  .id(id)
                                  .name("name")
                                  .path("path")
                                  .description("description")
                                  .universityId(UNIVERSITY_ID)
                                  .build();
    }

    private DocumentRequestDto createDocumentRequestDto() {
        return DocumentRequestDto.builder()
                                 .name("name")
                                 .description("description")
                                 .build();
    }
}

