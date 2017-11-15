package stroom.annotations;

import org.springframework.stereotype.Component;
import stroom.entity.shared.PermissionException;
import stroom.explorer.server.ExplorerActionHandler;
import stroom.query.api.v2.DocRef;
import stroom.security.SecurityContext;

@Component
public class StroomAnnotationsExplorerActionHandler implements ExplorerActionHandler {

    private final SecurityContext securityContext;

    public StroomAnnotationsExplorerActionHandler(final SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public DocRef createDocument(String name, String parentFolderUUID) {
        throw new PermissionException(securityContext.getUserId(), "You cannot create the Annotations node");
    }

    @Override
    public DocRef copyDocument(String uuid, String parentFolderUUID) {
        throw new PermissionException(securityContext.getUserId(), "You cannot copy the Annotations node");
    }

    @Override
    public DocRef moveDocument(String uuid, String parentFolderUUID) {
        throw new PermissionException(securityContext.getUserId(), "You cannot move the Annotations node");
    }

    @Override
    public DocRef renameDocument(String uuid, String name) {
        throw new PermissionException(securityContext.getUserId(), "You cannot rename the Annotations node");
    }

    @Override
    public void deleteDocument(String uuid) {
        throw new PermissionException(securityContext.getUserId(), "You cannot delete the Annotations node");
    }
}