# Sweden Geo Service

A Spring Boot service that provides official Swedish counties (län) and municipalities (kommuner).  
The data comes directly from [SCB – Statistics Sweden](https://www.scb.se), which is the official government source.

## Why?

In Sweden, many systems (healthcare, tax, education, government) need to work with län and kommun codes.  
These codes are the official identifiers and are used everywhere in the public sector.

This project downloads the official Excel file from SCB, parses it, and makes the data available through a simple REST API.

### Why download the Excel file?

SCB does not provide this dataset as a JSON or REST API.  
The official source is an Excel file (`.xls` or `.xlsx`) published on their website.  
At application startup, this service downloads that file, extracts the data, and keeps it in memory for fast API access.

## Features

- Fetch official 21 counties (län)
- Fetch all 290 municipalities (kommuner)
- Map between län and kommuner (each kommun always belongs to one län)
- Codes follow SCB’s official standard:
    - länskod: 2-digit county code (e.g. `01` = Stockholms län)
    - kommunkod: 4-digit municipality code (e.g. `0180` = Stockholm)

## Endpoints

- `GET /api/lan`  
  Returns all 21 counties (län).  
  Example:

```json
[
  {
    "lanCode": "01",
    "lanName": "Stockholms län"
  },
  {
    "lanCode": "03",
    "lanName": "Uppsala län"
  }
]
```
- ```GET /api/lan/{lanCode}/kommuner```

Returns all municipalities (kommuner) in the given län.

Example for 19 (Västmanlands län):
```json
[
  {
    "kommunCode": "1984",
    "kommunName": "Arboga",
    "lanCode": "19",
    "lanName": "Västmanlands län"
  },
  {
    "kommunCode": "1982",
    "kommunName": "Fagersta",
    "lanCode": "19",
    "lanName": "Västmanlands län"
  }
]
```
#### Tech Stack
* Java 21
* Maven
* Spring Boot
* spring boot web / Apache POI (to read the Excel .xls / .xlsx files from SCB)


#### Data Source

The dataset comes from SCB’s official file:

[Kommun- och länskoder](https://www.scb.se/hitta-statistik/regional-statistik-och-kartor/regionala-indelningar/lan-och-kommuner/lan-och-kommuner-i-kodnummerordning/)


It is updated whenever municipalities or counties change (rare, usually only at year boundaries).
### Build and run

```
mvn spring-boot:run
```

### Test the API 

```
curl http://localhost:8080/api/lan
curl http://localhost:8080/api/lan/01/kommuner
```
