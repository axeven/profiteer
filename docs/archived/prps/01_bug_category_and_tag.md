# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: Merge category and tag

**Priority**: High

**Estimated Complexity**: Medium

## 2. Context & Background
**Current State**: When create an income or expense transaction, user can input both tag and category

**Problem Statement**: Tag and category are actually the same concept. It could be a documentation issue where sometimes we mention tag but in other times we mention category

**User Impact**: Too many unnecessary input

**Business Value**: No real business value

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**: 
- Each transaction can be tagged with multiple tags
- Later, user will be able to filter transaction based on tags

**User Stories**: nothing much else to describe

### 3.3 UI/UX Requirements
**Design Specifications**:
- To enter tag input, it should be typed by user
- Once 3 characters are typed, there should be an auto complete suggestion based on historical data of tags that has been used before

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: [Initial implementation steps]
- Unify tag and category

**Phase 2**: [Follow-up implementation steps]
- Enable multiple tags per transaction

**Phase 3**: [Final implementation steps]
- Add tag auto completion

### 4.2 Files to Focus On
**Related Documentation**:
- `docs/concepts/transactions.md` - [Relevant concepts]

### 4.3 Dependencies & Prerequisites
**Required Before Starting**: nothing much

**Potential Blockers**: nothing much

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented
- [ ] Code follows project conventions and patterns
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] UI matches design specifications
- [ ] Documentation updated

### 5.2 Test Scenarios
**Happy Path**:
1. User open the app
2. User create physical wallet "Bank Account A" with balance 100
3. User create logical wallet "Expense" with balance 100
4. User open create income transaction dialog, and can see tag input. There will be no Category input
5. User create one income transaction with title "Test transaction", amount 10, tags: tag-a and tag-b, physical wallet "Bank Account A", logical wallet "Expense", with today as transaction date.
6. We expect the transaction data is created appropriately and physical and logical wallet balance is also updated to 110 for both.

### 5.3 Validation Methods
**Manual Testing**: will be done separately by developer

**Automated Testing**:
- [ ] a test that shows transaction supports multiple tags
- [ ] a test that shows transactions can be "Untagged"

## 6. Constraints & Considerations
nothing much

## 7. Risk Assessment
nothing much

## 8. Success Metrics
not sure

## 9. Future Considerations
nothing much
