# Transit Server-Side Walkthrough

Four different high-level concepts are fundamental to this application: vehicles, agencies, states, and countries.

These concepts share a strictly hierarchical relationship. Each vehicle falls under exactly one (of 67 possible) agencies. Each agency likewise falls under exactly one state, and each state falls under exactly one country.



## `agent` package


### `VehicleAgent`

NextBus Incorporated provides a publicly-available feed of transit data. The spec can be found [here](https://retro.umoiq.com/xmlFeedDocs/NextBusXMLFeed.pdf), and it details the various XML responses that one can receive from specified REST endpoints.

## `NextBusHttpAPI` class

The agent hierarchy from the previous section was modeled after this API.

## `model` package

A set of Plain Old Java Objects (POJOs) fundamental to the Swim server's logic.

These should be self-explanatory, as most of them simply wrap a handful of fields and lack non-getter and -setter methods. The only tricky piece is that any classes that are used as lane types within our Web Agents must be serializable/deserializable to/from Recon. Recall that a `swim.structure.Form` object has methods to store these rules; we generate `Forms` for all

Further reading: [Forms](/TODO).

