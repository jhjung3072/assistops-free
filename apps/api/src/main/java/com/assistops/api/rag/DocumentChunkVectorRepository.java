package com.assistops.api.rag;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentChunkVectorRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public DocumentChunkVectorRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void updateEmbedding(UUID chunkId, float[] embedding, String embeddingModel, Instant embeddedAt) {
		String sql = """
			UPDATE document_chunks
			SET embedding = CAST(:embedding AS vector),
			    embedded_at = :embeddedAt,
			    embedding_model = :embeddingModel
			WHERE id = :chunkId
			""";

		MapSqlParameterSource parameters = new MapSqlParameterSource()
			.addValue("chunkId", chunkId)
			.addValue("embedding", toVectorLiteral(embedding))
			.addValue("embeddingModel", embeddingModel)
			.addValue("embeddedAt", Timestamp.from(embeddedAt));

		jdbcTemplate.update(sql, parameters);
	}

	public List<ChunkSearchRow> searchSimilarChunks(
		Collection<UUID> workspaceIds,
		float[] queryEmbedding,
		int topK
	) {
		String sql = """
			SELECT
			    c.workspace_id,
			    c.document_id,
			    d.original_filename AS document_name,
			    c.id AS chunk_id,
			    c.chunk_index,
			    c.content,
			    (c.embedding <=> CAST(:queryEmbedding AS vector)) AS distance,
			    (1 - (c.embedding <=> CAST(:queryEmbedding AS vector))) AS score,
			    c.embedding_model
			FROM document_chunks c
			JOIN documents d ON d.id = c.document_id
			WHERE c.workspace_id IN (:workspaceIds)
			  AND c.embedding IS NOT NULL
			  AND d.status <> 'DELETED'
			ORDER BY c.embedding <=> CAST(:queryEmbedding AS vector)
			LIMIT :topK
			""";

		MapSqlParameterSource parameters = new MapSqlParameterSource()
			.addValue("workspaceIds", workspaceIds)
			.addValue("queryEmbedding", toVectorLiteral(queryEmbedding))
			.addValue("topK", topK);

		return jdbcTemplate.query(sql, parameters, (resultSet, rowNumber) -> new ChunkSearchRow(
			resultSet.getObject("workspace_id", UUID.class),
			resultSet.getObject("document_id", UUID.class),
			resultSet.getString("document_name"),
			resultSet.getObject("chunk_id", UUID.class),
			resultSet.getInt("chunk_index"),
			resultSet.getString("content"),
			resultSet.getDouble("score"),
			resultSet.getDouble("distance"),
			resultSet.getString("embedding_model")
		));
	}

	private String toVectorLiteral(float[] embedding) {
		StringBuilder builder = new StringBuilder("[");

		for (int index = 0; index < embedding.length; index++) {
			if (index > 0) {
				builder.append(',');
			}
			builder.append(Float.toString(embedding[index]));
		}

		return builder.append(']').toString();
	}

	public record ChunkSearchRow(
		UUID workspaceId,
		UUID documentId,
		String documentName,
		UUID chunkId,
		int chunkIndex,
		String content,
		double score,
		double distance,
		String embeddingModel
	) {
	}
}
