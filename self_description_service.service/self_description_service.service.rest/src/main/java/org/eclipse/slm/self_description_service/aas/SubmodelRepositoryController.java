package org.eclipse.slm.self_description_service.aas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationRequest;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifierSize;
import org.eclipse.digitaltwin.basyx.http.pagination.Base64UrlEncodedCursor;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResult;
import org.eclipse.digitaltwin.basyx.http.pagination.PagedResultPagingMetadata;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.SubmodelRepositoryHTTPApi;
import org.eclipse.digitaltwin.basyx.submodelrepository.http.pagination.GetSubmodelsResult;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.slm.self_description_service.aas.exceptions.MethodNotImplementedException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
public class SubmodelRepositoryController implements SubmodelRepositoryHTTPApi {


    private final org.eclipse.slm.self_description_service.aas.SubmodelRepository repository;

    public SubmodelRepositoryController(SubmodelRepository repository) {
        this.repository = repository;
    }

    private String getEncodedCursorFromCursorResult(CursorResult<?> cursorResult) {
        if (cursorResult == null || cursorResult.getCursor() == null) {
            return null;
        }

        return Base64UrlEncodedCursor.encodeCursor(cursorResult.getCursor());
    }


    @Override
    public ResponseEntity<PagedResult> getAllSubmodels(@Base64UrlEncodedIdentifierSize(min = 1, max = 3072) @Valid Base64UrlEncodedIdentifier semanticId, @Valid String idShort, @Min(1L) @Valid Integer limit, @Valid Base64UrlEncodedCursor cursor, @Valid String level, @Valid String extent) {
        if (limit == null) {
            limit = 100;
        }

        String decodedCursor = "";
        if (cursor != null) {
            decodedCursor = cursor.getDecodedCursor();
        }

        PaginationInfo pInfo = new PaginationInfo(limit, decodedCursor);

        CursorResult<List<Submodel>> cursorResult = repository.getAllSubmodels(pInfo);

        GetSubmodelsResult paginatedSubmodel = new GetSubmodelsResult();

        String encodedCursor = getEncodedCursorFromCursorResult(cursorResult);

        paginatedSubmodel.result(new ArrayList<>(cursorResult.getResult()));
        paginatedSubmodel.setPagingMetadata(new PagedResultPagingMetadata().cursor(encodedCursor));

        return new ResponseEntity<PagedResult>(paginatedSubmodel, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelById(Base64UrlEncodedIdentifier submodelIdentifier, @Valid String level, @Valid String extent) {
        return new ResponseEntity<Submodel>(repository.getSubmodel(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SubmodelValueOnly> getSubmodelByIdValueOnly(Base64UrlEncodedIdentifier submodelIdentifier, @Valid String level, @Valid String extent) {
        return new ResponseEntity<SubmodelValueOnly>(repository.getSubmodelByIdValueOnly(submodelIdentifier.getIdentifier()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Submodel> getSubmodelByIdMetadata(Base64UrlEncodedIdentifier submodelIdentifier, @Valid String level) {
        return new ResponseEntity<Submodel>(repository.getSubmodelByIdMetadata(submodelIdentifier.getIdentifier()), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<Submodel> postSubmodel(@Valid Submodel body) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> putSubmodelById(Base64UrlEncodedIdentifier submodelIdentifier, @Valid Submodel body, @Valid String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelById(Base64UrlEncodedIdentifier submodelIdentifier) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<PagedResult> getAllSubmodelElements(Base64UrlEncodedIdentifier submodelIdentifier, @Min(1L) @Valid Integer limit, @Valid Base64UrlEncodedCursor cursor, @Valid String level, @Valid String extent) {
        return null;
    }

    @Override
    public ResponseEntity<SubmodelElement> getSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid String level, @Valid String extent) {
        return null;
    }

    @Override
    public ResponseEntity<SubmodelElementValue> getSubmodelElementByPathValueOnlySubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid String level, @Valid String extent) {
        return null;
    }

    @Override
    public ResponseEntity<Void> patchSubmodelElementByPathValueOnlySubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid SubmodelElementValue body, @Valid String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<SubmodelElement> postSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid SubmodelElement body, @Valid String level, @Valid String extent) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<SubmodelElement> postSubmodelElementSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, @Valid SubmodelElement body) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Resource> getFileByPath(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        return null;
    }

    @Override
    public ResponseEntity<Void> putFileByPath(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, String fileName, @Valid MultipartFile file) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> deleteFileByPath(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<OperationResult> invokeOperationSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid OperationRequest body, @Valid Boolean async) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> putSubmodelElementByPathSubmodelRepo(Base64UrlEncodedIdentifier submodelIdentifier, String idShortPath, @Valid SubmodelElement body, @Valid String level) {
        throw new MethodNotImplementedException();
    }

    @Override
    public ResponseEntity<Void> patchSubmodelByIdValueOnly(Base64UrlEncodedIdentifier submodelIdentifier, @Valid List<SubmodelElement> body, @Valid String level) {
        throw new MethodNotImplementedException();
    }
}
