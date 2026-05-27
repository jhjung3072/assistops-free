CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    created_by UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_workflows_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workflows_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE workflow_nodes (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    node_key VARCHAR(120) NOT NULL,
    type VARCHAR(40) NOT NULL,
    label VARCHAR(255) NOT NULL,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    config_json JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_nodes_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE CASCADE,
    CONSTRAINT uq_workflow_nodes_workflow_node_key UNIQUE (workflow_id, node_key)
);

CREATE TABLE workflow_edges (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    edge_key VARCHAR(120) NOT NULL,
    source_node_key VARCHAR(120) NOT NULL,
    target_node_key VARCHAR(120) NOT NULL,
    label VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_edges_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id) ON DELETE CASCADE,
    CONSTRAINT uq_workflow_edges_workflow_edge_key UNIQUE (workflow_id, edge_key)
);

CREATE TABLE workflow_runs (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    triggered_by UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    input_json JSONB,
    output_json JSONB,
    error_message TEXT,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_runs_workflow FOREIGN KEY (workflow_id) REFERENCES workflows (id),
    CONSTRAINT fk_workflow_runs_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workflow_runs_triggered_by FOREIGN KEY (triggered_by) REFERENCES users (id)
);

CREATE TABLE workflow_run_steps (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL,
    node_key VARCHAR(120) NOT NULL,
    node_type VARCHAR(40) NOT NULL,
    label VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    input_json JSONB,
    output_json JSONB,
    error_message TEXT,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_run_steps_run FOREIGN KEY (run_id) REFERENCES workflow_runs (id) ON DELETE CASCADE
);

CREATE INDEX idx_workflows_workspace_updated_at ON workflows (workspace_id, updated_at DESC);
CREATE INDEX idx_workflow_runs_workflow_created_at ON workflow_runs (workflow_id, created_at DESC);
CREATE INDEX idx_workflow_run_steps_run_created_at ON workflow_run_steps (run_id, created_at ASC);
