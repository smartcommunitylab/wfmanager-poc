import {
  Admin,
  EditGuesser,
  ListGuesser,
  Resource,
  ShowGuesser,
} from "react-admin";
import { Layout } from "./Layout";
import { dataProvider } from "./dataProvider";
import { WorkflowCreate, WorkflowList, WorkflowShow } from "./workflows";
import { TaskShow } from "./tasks";

export const App = () => (
  <Admin layout={Layout} dataProvider={dataProvider}>
    <Resource
      name="workflow"
      list={<WorkflowList />}
      show={<WorkflowShow />}
      create={<WorkflowCreate />}
    />
    <Resource name="task" show={<TaskShow />} />
  </Admin>
);
