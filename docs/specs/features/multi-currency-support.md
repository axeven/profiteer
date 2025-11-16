# Multi-Currency Support

**Status**: ✅ Implemented
**Last Updated**: 2025-11-16

## Purpose

Support diverse financial instruments including standard currencies, precious metals, and cryptocurrency with accurate conversion and display.

## Supported Currency Types

- **Standard Currencies**: USD, EUR, GBP, JPY, CAD, AUD, IDR (2-decimal precision)
- **Precious Metals**: GOLD with gram-based pricing (3-decimal precision)
- **Cryptocurrency**: BTC with satoshi-level accuracy (8-decimal precision)

## Currency Conversion System

- **Default Currency**: User-configurable base currency for aggregated displays
- **Dual Rate System**: Default rates + Monthly rates (monthly takes precedence)
- **Bi-directional Conversion**: Automatic inverse rate calculation
- **Missing Rate Warnings**: User alerts when conversion rates needed

## Related Specifications

- [Currency Domain Model](../domain/currencies.md)
- [Database Schema](../technical/database-schema.md)
- [Architecture Overview](../architecture/overview.md)

---

**Last Reviewed**: 2025-11-16
