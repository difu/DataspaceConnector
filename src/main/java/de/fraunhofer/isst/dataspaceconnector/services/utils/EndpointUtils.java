package de.fraunhofer.isst.dataspaceconnector.services.utils;

import de.fraunhofer.isst.dataspaceconnector.model.EndpointId;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

public final class EndpointUtils {
    private EndpointUtils() {
    }

    /**
     * Determines the current endpoint id from the request context.
     *
     * @param resourceId The resource id passed along the request.
     * @return The Endpoint id.
     */
    public static EndpointId getCurrentEndpoint(final UUID resourceId) {
        var basePath = getCurrentRequestUriBuilder().build().toString();

        final var index = basePath.lastIndexOf(resourceId.toString()) - 1;
        // -1 so that the / gets also removed
        basePath = basePath.substring(0, index);

        return new EndpointId(basePath, resourceId);
    }

    public static EndpointId getEndpointIdFromPath(final URI uri) {
        final var fullPath = uri.toString();
        final var allUuids = UUIDUtils.findUuids(fullPath);

        final var resourceId = UUID.fromString(allUuids.get(0));
        final var index = fullPath.lastIndexOf(resourceId.toString()) - 1;
        // -1 so that the / gets also removed
        final var basePath = fullPath.substring(0, index);

        return new EndpointId(basePath, resourceId);
    }

    public static URI getCurrentBasePath() {
        return getCurrentRequestUriBuilder().build().toUri();
    }

    private static ServletUriComponentsBuilder getCurrentRequestUriBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }
}
