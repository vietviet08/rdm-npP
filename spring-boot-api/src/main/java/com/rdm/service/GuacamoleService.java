package com.rdm.service;

import com.rdm.model.Device;
import com.rdm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class GuacamoleService {

    private static final Logger logger = LoggerFactory.getLogger(GuacamoleService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Value("${guacamole.url:http://localhost/guacamole}")
    private String guacamoleBaseUrl;

    // Service account username for Guacamole
    @Value("${guacamole.service-account:rdm-service}")
    private String serviceAccountUsername;

    public GuacamoleService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Create a Guacamole connection for a device
     * 
     * @param device The device to create connection for
     * @return The Guacamole connection ID
     */
    @Transactional
    public String createGuacamoleConnection(Device device) {
        logger.info("Creating Guacamole connection for device: {}", device.getName());

        try (Connection conn = dataSource.getConnection()) {
            // Get the protocol name for Guacamole
            String protocol = mapProtocolToGuacamole(device.getProtocol());

            // Insert into guacamole_connection table
            String insertConnectionSql = "INSERT INTO guacamole_connection (connection_name, parent_id, protocol) " +
                    "VALUES (?, (SELECT connection_group_id FROM guacamole_connection_group WHERE connection_group_name = 'ROOT'), ?) "
                    +
                    "RETURNING connection_id";

            Integer connectionId;
            try (PreparedStatement stmt = conn.prepareStatement(insertConnectionSql)) {
                stmt.setString(1, device.getName());
                stmt.setString(2, protocol);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        connectionId = rs.getInt(1);
                    } else {
                        throw new RuntimeException("Failed to create Guacamole connection");
                    }
                }
            }

            // Insert connection parameters
            Map<String, String> parameters = buildConnectionParameters(device);
            insertConnectionParameters(conn, connectionId, parameters);

            // Grant permission to service account
            grantConnectionPermission(conn, connectionId, serviceAccountUsername);

            logger.info("Created Guacamole connection with ID: {} for device: {}", connectionId, device.getName());
            return String.valueOf(connectionId);

        } catch (SQLException e) {
            logger.error("Error creating Guacamole connection for device: {}", device.getName(), e);
            throw new RuntimeException("Failed to create Guacamole connection", e);
        }
    }

    /**
     * Update a Guacamole connection for a device
     * 
     * @param device The device with updated information
     */
    @Transactional
    public void updateGuacamoleConnection(Device device) {
        if (device.getGuacamoleConnId() == null || device.getGuacamoleConnId().isEmpty()) {
            logger.warn("Device {} has no Guacamole connection ID, creating new connection", device.getName());
            String connId = createGuacamoleConnection(device);
            // Note: DeviceService should update the device with the new connection ID
            return;
        }

        logger.info("Updating Guacamole connection {} for device: {}", device.getGuacamoleConnId(), device.getName());

        try (Connection conn = dataSource.getConnection()) {
            Integer connectionId = Integer.parseInt(device.getGuacamoleConnId());

            // Update connection name
            String updateConnectionSql = "UPDATE guacamole_connection SET connection_name = ?, protocol = ? WHERE connection_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateConnectionSql)) {
                stmt.setString(1, device.getName());
                stmt.setString(2, mapProtocolToGuacamole(device.getProtocol()));
                stmt.setInt(3, connectionId);
                stmt.executeUpdate();
            }

            // Delete existing parameters
            String deleteParamsSql = "DELETE FROM guacamole_connection_parameter WHERE connection_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteParamsSql)) {
                stmt.setInt(1, connectionId);
                stmt.executeUpdate();
            }

            // Insert updated parameters
            Map<String, String> parameters = buildConnectionParameters(device);
            insertConnectionParameters(conn, connectionId, parameters);

            logger.info("Updated Guacamole connection {} for device: {}", connectionId, device.getName());

        } catch (SQLException e) {
            logger.error("Error updating Guacamole connection for device: {}", device.getName(), e);
            throw new RuntimeException("Failed to update Guacamole connection", e);
        }
    }

    /**
     * Delete a Guacamole connection
     * 
     * @param guacamoleConnId The Guacamole connection ID
     */
    @Transactional
    public void deleteGuacamoleConnection(String guacamoleConnId) {
        if (guacamoleConnId == null || guacamoleConnId.isEmpty()) {
            logger.warn("No Guacamole connection ID provided for deletion");
            return;
        }

        logger.info("Deleting Guacamole connection: {}", guacamoleConnId);

        try (Connection conn = dataSource.getConnection()) {
            Integer connectionId = Integer.parseInt(guacamoleConnId);

            // Delete connection parameters first (foreign key constraint)
            String deleteParamsSql = "DELETE FROM guacamole_connection_parameter WHERE connection_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteParamsSql)) {
                stmt.setInt(1, connectionId);
                stmt.executeUpdate();
            }

            // Delete connection permissions
            String deletePermsSql = "DELETE FROM guacamole_connection_permission WHERE connection_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePermsSql)) {
                stmt.setInt(1, connectionId);
                stmt.executeUpdate();
            }

            // Delete the connection
            String deleteConnectionSql = "DELETE FROM guacamole_connection WHERE connection_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteConnectionSql)) {
                stmt.setInt(1, connectionId);
                stmt.executeUpdate();
            }

            logger.info("Deleted Guacamole connection: {}", connectionId);

        } catch (SQLException e) {
            logger.error("Error deleting Guacamole connection: {}", guacamoleConnId, e);
            throw new RuntimeException("Failed to delete Guacamole connection", e);
        }
    }

    /**
     * Get Guacamole connection URL for a user to access
     * 
     * @param guacamoleConnId The Guacamole connection ID
     * @param userId          The user ID (for authentication)
     * @return The URL to access the connection
     */
    public String getGuacamoleConnectionUrl(String guacamoleConnId, Integer userId) {
        // Ensure Guacamole user exists for this app user
        ensureGuacamoleUserExists(userId);

        // Build URL: /guacamole/#/client/{connectionId}
        // Note: Guacamole will handle authentication via session or token
        return guacamoleBaseUrl + "/#/client/" + guacamoleConnId;
    }

    /**
     * Ensure a Guacamole user exists for the app user
     * 
     * @param userId The app user ID
     */
    @Transactional
    public void ensureGuacamoleUserExists(Integer userId) {
        // For now, we'll use a service account approach
        // In a more sophisticated implementation, we could create individual users
        // or use Guacamole's REST API with authentication headers

        // This is a placeholder - actual implementation depends on authentication
        // strategy
        logger.debug("Ensuring Guacamole user exists for app user: {}", userId);
    }

    /**
     * Map device protocol to Guacamole protocol name
     */
    private String mapProtocolToGuacamole(Device.Protocol protocol) {
        return switch (protocol) {
            case rdp -> "rdp";
            case vnc -> "vnc";
            case ssh -> "ssh";
        };
    }

    /**
     * Build connection parameters based on device and protocol
     */
    private Map<String, String> buildConnectionParameters(Device device) {
        Map<String, String> params = new HashMap<>();

        switch (device.getProtocol()) {
            case rdp:
                params.put("hostname", device.getHost());
                params.put("port", String.valueOf(device.getPort()));
                if (device.getUsername() != null) {
                    params.put("username", device.getUsername());
                }
                if (device.getPasswordEncrypted() != null) {
                    // Decrypt password (for now, it's plaintext)
                    params.put("password", device.getPasswordEncrypted());
                }
                params.put("security", "any");
                params.put("ignore-cert", "true");
                params.put("enable-wallpaper", "false");
                params.put("enable-theming", "false");
                params.put("enable-font-smoothing", "true");
                params.put("enable-full-window-drag", "true");
                params.put("enable-desktop-composition", "true");
                params.put("enable-menu-animations", "true");
                params.put("disable-bitmap-caching", "false");
                params.put("disable-offscreen-caching", "false");
                params.put("disable-glyph-caching", "false");
                break;

            case vnc:
                params.put("hostname", device.getHost());
                params.put("port", String.valueOf(device.getPort()));
                if (device.getUsername() != null) {
                    params.put("username", device.getUsername());
                }
                if (device.getPasswordEncrypted() != null) {
                    params.put("password", device.getPasswordEncrypted());
                }
                params.put("color-depth", "24");
                params.put("dpi", "96");
                break;

            case ssh:
                params.put("hostname", device.getHost());
                params.put("port", String.valueOf(device.getPort()));
                if (device.getUsername() != null) {
                    params.put("username", device.getUsername());
                }
                if (device.getPasswordEncrypted() != null) {
                    params.put("password", device.getPasswordEncrypted());
                }
                if (device.getPrivateKey() != null) {
                    params.put("private-key", device.getPrivateKey());
                }
                params.put("font-name", "monospace");
                params.put("font-size", "12");
                params.put("color-scheme", "gray-black");
                break;
        }

        return params;
    }

    /**
     * Insert connection parameters into guacamole_connection_parameter table
     */
    private void insertConnectionParameters(Connection conn, Integer connectionId, Map<String, String> parameters)
            throws SQLException {
        String insertParamSql = "INSERT INTO guacamole_connection_parameter (connection_id, parameter_name, parameter_value) "
                +
                "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertParamSql)) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                stmt.setInt(1, connectionId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Grant connection permission to a user
     */
    private void grantConnectionPermission(Connection conn, Integer connectionId, String username)
            throws SQLException {
        // Get user entity ID
        String getUserSql = "SELECT entity_id FROM guacamole_entity WHERE name = ? AND type = 'USER'";
        Integer entityId = null;

        try (PreparedStatement stmt = conn.prepareStatement(getUserSql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    entityId = rs.getInt(1);
                }
            }
        }

        if (entityId == null) {
            logger.warn("Guacamole user {} not found, skipping permission grant", username);
            return;
        }

        // Grant READ and UPDATE permissions (READ = 1, UPDATE = 2)
        String grantPermSql = "INSERT INTO guacamole_connection_permission (entity_id, connection_id, permission) " +
                "VALUES (?, ?, ?) ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = conn.prepareStatement(grantPermSql)) {
            // READ permission
            stmt.setInt(1, entityId);
            stmt.setInt(2, connectionId);
            stmt.setString(3, "READ");
            stmt.addBatch();

            // UPDATE permission (allows connection)
            stmt.setInt(1, entityId);
            stmt.setInt(2, connectionId);
            stmt.setString(3, "UPDATE");
            stmt.addBatch();

            stmt.executeBatch();
        }
    }
}
