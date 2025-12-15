import {
  DateField,
  NumberField,
  ReferenceField,
  Show,
  SimpleShowLayout,
  TextField,
} from "react-admin";
import { StatusChip } from "./workflows";
import { Divider, Stack } from "@mui/material";

export const TaskShow = () => (
  <Show>
    <SimpleShowLayout>
      <Stack direction={"row"} gap={1}>
        <ReferenceField source="workflowId" reference="workflow" />
        {">"}
        <TextField source="id" />
      </Stack>
      <TextField source="type" />
      <Divider />
      <NumberField source="version" />
      <StatusChip source="status" />
      <DateField source="createdAt" transform={(v) => new Date(v)} />
      <DateField source="updatedAt" transform={(v) => new Date(v)} />
    </SimpleShowLayout>
  </Show>
);
