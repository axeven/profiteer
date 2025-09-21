# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Currency symbol does not follow actual currency

**Priority**: Low

**Estimated Complexity**: Simple

## 2. Context & Background
**Current State**: 
- In transaction create and edit dialog, currency symbol is always $ instead of following the expected currency. 
- In the home page, in the list of recent transaction, the currency symbol is always $ instead of following the expected currency.

**Problem Statement**: The miss match between the currency set by user and the currency being displayed is bringing confusion to user.

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- Currency symbol should follow the currency set by user
- If currency symbol does not exist, then just show the currency text, e.g. IDR, USD, etc.

### 3.2 Technical Requirements

### 3.3 UI/UX Requirements
**Design Specifications**:
- If currency text is too long, it could affect how the transaction amount being displayed. As much as possible avoid to wrap the transaction amount. Instead of wrapping the amount, consider to either round the decimals to integer, or substitute the thousands digit with letters like "K" for one thousand multiple. Or "M" for one million multiple. For example, 1200000 IDR can either be shown as 1200K IDR or 1.2M IDR
