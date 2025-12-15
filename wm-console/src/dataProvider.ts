// SPDX-FileCopyrightText: © 2025 DSLab - Fondazione Bruno Kessler
//
// SPDX-License-Identifier: Apache-2.0

import { stringify } from "query-string";
import { fetchUtils, DataProvider } from "ra-core";
import { GetListParams, Options } from "react-admin";

/**
 * Data Provider for Spring REST with Pageable support.
 * List/ManyReference expects a page in return, and will send paging/sorting parameters,
 * while Many will expect a list in a wrapper as response, and won't use paging/sorting at all
 *
 *
 * @param apiUrl
 * @param httpClient
 *
 * @example
 *
 * getList          => GET http://apiUrl/data?page=1&sort=id,ASC
 * getOne           => GET http://apiUrl/data/<id>
 * getManyReference => GET http://apiUrl/data?field=<id>
 * getMany          => GET http://apiUrl/data?id=<id>,<id>
 * create           => POST http://apiUrl/data
 * update           => PUT http://apiUrl/data/<id>
 * updateMany       => PUT http://apiUrl/data/<id>, PUT http://apiUrl/data/<id>
 * delete           => DELETE http://apiUrl/data/<id>
 *
 */

const springDataProvider = (apiUrl: string): DataProvider => {
  const httpClient: (
    url: any,
    options?: fetchUtils.Options | undefined,
  ) => Promise<{
    status: number;
    headers: Headers;
    body: string;
    json: any;
  }> = fetchUtils.fetchJson;

  return {
    getList: (resource, params) => {
      //handle pagination request as pageable (page,size)
      const { page, perPage } = params.pagination || {
        page: 1,
        perPage: 10,
      };
      const { field, order } = params.sort || {
        field: "id",
        order: "ASC",
      };

      const query = {
        ...fetchUtils.flattenObject(params.filter), //additional filter parameters as-is
        sort: field + "," + order, //sorting
        page: page - 1, //page starts from zero
        size: perPage,
      };

      const url = `${apiUrl}/${resource}?${stringify(query)}`;

      return httpClient(url).then(({ status, json }) => {
        if (status !== 200) {
          throw new Error("Invalid response status " + status);
        }
        if (!json.content) {
          throw new Error("the response must match page<> model");
        }

        //extract data from content
        return {
          data: json.content,
          total: parseInt(json.totalElements),
        };
      });
    },
    //get the specific version based on id. The Url changes and it
    getOne: (resource, params) => {
      const url = `${apiUrl}/${resource}/${params.id}`;
      return httpClient(url).then(({ status, json }) => {
        if (status !== 200) {
          throw new Error("Invalid response status " + status);
        }
        return {
          data: json,
        };
      });
    },
    getMany: (resource, params) => {
      const url = `${apiUrl}/${resource}`;

      //make a distinct call for every entry
      return Promise.all(
        params.ids.map((id) => httpClient(`${url}/${id}`)),
      ).then((responses) => ({
        data: responses.map(({ json }) => json),
        total: responses.length,
      }));
    },
    getManyReference: (resource, params) => {
      const url = `${apiUrl}/${resource}/${params.id}`;
      return httpClient(url).then(({ status, json }) => {
        if (status !== 200) {
          throw new Error("Invalid response status " + status);
        }
        return {
          data: json,
        };
      });
    },
    update: (resource, params) => {
      if (!params.data) {
        throw new Error("Invalid data");
      }

      const url = `${apiUrl}/${resource}/${params.id}`;
      return httpClient(url, {
        method: "PUT",
        body:
          typeof params.data === "string"
            ? params.data
            : JSON.stringify(params.data),
      }).then(({ json }) => ({ data: json }));
    },
    updateMany: (resource, params) => {
      const url = `${apiUrl}/${resource}`;

      //make a distinct call for every entry
      return Promise.all(
        params.ids.map((id) =>
          httpClient(`${url}/${id}`, {
            method: "PUT",
            body: JSON.stringify(params.data),
          }),
        ),
      ).then((responses) => ({
        data: responses.map(({ json }) => json.id),
      }));
    },
    create: (resource, params) => {
      const url = `${apiUrl}/${resource}`;

      return httpClient(url, {
        method: "POST",
        body:
          typeof params.data === "string"
            ? params.data
            : JSON.stringify(params.data),
      }).then(({ json }) => ({
        data: { ...json, id: json.id || "" } as any,
      }));
    },
    delete: (resource, params) => {
      const url = `${apiUrl}/${resource}/${params.id}`;

      return httpClient(url, {
        method: "DELETE",
      }).then(({ json }) => ({ data: json }));
    },
    deleteMany: (resource, params) => {
      const url = `${apiUrl}/${resource}`;

      const promises = params.ids.map((id) =>
        httpClient(`${url}/${id}`, {
          method: "DELETE",
        }),
      );

      //make a distinct call for every entry
      return Promise.all(promises).then((responses) => ({
        data: responses.map(({ json }) => json),
      }));
    },
  };
};

export const dataProvider = springDataProvider(
  import.meta.env.VITE_SIMPLE_REST_URL + "/api",
);

export default springDataProvider;
