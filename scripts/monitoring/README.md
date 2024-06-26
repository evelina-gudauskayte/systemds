<!--
{% comment %}
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to you under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
{% end comment %}
-->

# Frontend for monitoring tool of federated infrastrucuture

A frontend application, used to visualize and manipulate the backend application functionality of the monitoring tool

## Backend requirements

A running instance of the backend application is required for the frontend to function, default port of the backend is **8080**.

## Install & Run

To install and run the app do the following:

```bash
  cd scripts/monitoring
  npm install
  npm run start
```

To view the app Navigate to `http://localhost:4200/`.

## Running Tests

To run tests, run the following command:

```bash
  npm run test
```

## API Reference

Get all registered workers

```http
  GET /workers
```

Get specific worker

```http
  GET /workers/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the worker to fetch |

Register worker for monitoring

```http
  POST /workers
```

Request body in **JSON** format:

| Body parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `name`    | `string` | **Required**. Name of the worker to register |
| `address` | `string` | **Required**. Address of the worker to register |

Example:

```json
{
  "name": "Worker 1",
  "address": "localhost:8001"
}
```

Edit registered worker

```http
  PUT /workers/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the worker to edit |

Request body in **JSON** format:

| Body parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `name`    | `string` | Changed name of the worker |
| `address` | `string` | Changed address of the worker |

Example:

```json
{
  "name": "Worker 42",
  "address": "localhost:8005"
}
```

Deregister specific worker

```http
  DELETE /workers/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the worker to deregister |

---

Get all registered coordinators

```http
  GET /coordinators
```

Get specific coordinator

```http
  GET /coordinators/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the coordinator to fetch |

Register coordinator for monitoring

```http
  POST /coordinators
```

Request body in **JSON** format:

| Body parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `name`    | `string` | **Required**. Name of the coordinator to register |
| `address` | `string` | **Required**. Address of the coordinator to register |

Example:

```json
{
  "name": "Coordinator 1",
  "address": "localhost:8441"
}
```

Edit registered coordinator

```http
  PUT /coordinators/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the coordinator to edit |

Request body in **JSON** format:

| Body parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `name`    | `string` | Changed name of the coordinator |
| `address` | `string` | Changed address of the coordinator |

Example:

```json
{
  "name": "Coordinator 4",
  "address": "localhost:8445"
}
```

Deregister specific coordinator

```http
  DELETE /coordinators/${id}
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `id`      | `int` | **Required**. Id of the coordinator to deregister |
