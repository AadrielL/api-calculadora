# Calculator API Documentation

This documentation provides a comprehensive overview of the Calculator API used in the project **api-calculadora**. The API serves as the main orchestrator of calculation functionality, ensuring that requests are processed efficiently and accurately.

## Authentication Flow

The Calculator API employs a token-based authentication mechanism. Users must obtain an access token by providing valid credentials to the authentication endpoint. The access token must be included in the header of each request.

### Steps to Authenticate:
1. **Request Token**: Send a POST request to `/auth/login` with the username and password.
2. **Receive Token**: On successful authentication, the server responds with a JSON object containing the access token.
3. **Use Token**: Include the token in the `Authorization` header for subsequent requests.

```json
Authorization: Bearer <access_token>
```

## NBR 5410 Engine Integration

The API integrates with the NBR 5410 engines to perform electrical calculations, adhering to the Brazilian standard. The integration allows for the necessary handling of electrical parameters and ensures compliance with regulatory guidelines.

### Example Request:
```json
POST /calculate/nbr5410
{
  "voltage": 220,
  "current": 10,
  "powerFactor": 0.8
}
```

### Example Response:
```json
{
  "result": {
    "activePower": 1760,
    "reactivePower": 0
  }
}
```

## Multitenancy Strategy

To support multiple tenants within a single instance of the application, the Calculator API utilizes a multitenancy architecture that isolates data for different users. Each tenant's data is identified using a unique tenant ID.

### Implementation:
- Each request must include a `Tenant-ID` header to route data appropriately.
- The API ensures that queries are filtered based on the tenant's ID, preventing data leakage between tenants.

## Complete System Architecture

The following diagram illustrates the system architecture of the Calculator API:

```plaintext
                +-------------------+
                |    Client App     |
                +---------+---------+
                          |
                  +-------v-------+
                  |    API Gateway  |
                  +-------+-------+
                          |
                +---------+---------+
                |                   |
        +-------v------+   +-------v-------+
        | Authentication |   |   Calculation  |
        |     Module     |   |     Module     |
        +---------------+   +---------------+

```

### Code Examples

#### Sample Code for Using the API:
```python
import requests

# Authenticate
response = requests.post('https://api.calculator.com/auth/login', json={
    'username': 'user',
    'password': 'pass'
})
access_token = response.json()['access_token']

# Make a Calculation request
headers = {'Authorization': f'Bearer {access_token}', 'Tenant-ID': 'tenant_123'}
response = requests.post('https://api.calculator.com/calculate/nbr5410', headers=headers, json={
    'voltage': 220,
    'current': 10,
    'powerFactor': 0.8
})
print(response.json())
```

This documentation will be updated regularly to reflect changes in the API and to provide more examples and information as needed.