import {
  Create,
  SimpleForm,
  TextInput,
  required,
  ArrayInput,
  SimpleFormIterator,
  FunctionField,
  ArrayField,
  Show,
  SimpleShowLayout,
  TextField,
  DateField,
  ChipField,
  useRecordContext,
  ResourceContextProvider,
  BooleanInput,
  BooleanField,
} from "react-admin";
import { DataTable, List } from "react-admin";

function statusToColor(
  value: string,
):
  | "info"
  | "warning"
  | "success"
  | "error"
  | "default"
  | "primary"
  | "secondary" {
  const map = {
    PENDING: "info",
    IN_PROGRESS: "warning",
    COMPLETED: "success",
    FAILED: "error",
  };

  return map[value] ?? "default";
}

export const StatusChip = (props: { source: string }) => {
  const record = useRecordContext();
  if (!record || !record.status) {
    return <></>;
  }

  const color = statusToColor(record.status);

  return <ChipField source="status" color={color} />;
};

export const WorkflowCreate = () => {
  const r = {
    id: self.crypto.randomUUID(),
  };

  return (
    <Create record={r} redirect="list">
      <SimpleForm>
        <TextInput source="id" readOnly />
        <TextInput source="name" validate={[required()]} />
        <ArrayInput source="tasks">
          <SimpleFormIterator inline>
            <TextInput
              source="type"
              helperText={false}
              validate={[required()]}
            />
          </SimpleFormIterator>
        </ArrayInput>
        <BooleanInput source="parallel" defaultChecked={false} />
      </SimpleForm>
    </Create>
  );
};

export const WorkflowList = () => (
  <List exporter={false}>
    <DataTable bulkActionButtons={false}>
      <DataTable.Col source="id" />
      <DataTable.Col source="name" />
      <DataTable.Col source="tasks">
        <FunctionField render={(r) => (r.tasks ? r.tasks.length : 0)} />
      </DataTable.Col>
    </DataTable>
  </List>
);

export const WorkflowShow = () => (
  <Show>
    <SimpleShowLayout>
      <TextField source="id" />
      <TextField source="name" />
      <ArrayField source="tasks">
        <ResourceContextProvider value="task">
          <DataTable bulkActionButtons={false}>
            <DataTable.Col source="id">
              <TextField source="id" />
            </DataTable.Col>
            <DataTable.Col source="type">
              <TextField source="type" />
            </DataTable.Col>
            <DataTable.Col source="status">
              <StatusChip source="status" />
            </DataTable.Col>
            <DataTable.Col source="createdAt">
              <DateField source="createdAt" transform={(v) => new Date(v)} />
            </DataTable.Col>
            <DataTable.Col source="updatedAt">
              <DateField source="updatedAt" transform={(v) => new Date(v)} />
            </DataTable.Col>
          </DataTable>
        </ResourceContextProvider>
      </ArrayField>
      <BooleanField source="parallel" />
    </SimpleShowLayout>
  </Show>
);
