# Product Requirement Prompt (PRP) Template

## 1. Overview
**Brief Description**: [One sentence summary of what needs to be built/changed]

**Priority**: [High/Medium/Low]

**Estimated Complexity**: [Simple/Medium/Complex]

**Expected Development Time**: [e.g., 1-2 hours, half day, 1-2 days]

## 2. Context & Background
**Current State**: [Describe how things work now, if applicable]

**Problem Statement**: [What problem are we solving? Why is this needed?]

**User Impact**: [Who benefits and how?]

**Business Value**: [Why is this important for the product?]

## 3. Detailed Requirements

### 3.1 Functional Requirements
**Core Features**:
- [ ] Feature 1: [Specific, measurable requirement]
- [ ] Feature 2: [Specific, measurable requirement]
- [ ] Feature 3: [Specific, measurable requirement]

**User Stories**:
- As a [user type], I want [functionality] so that [benefit]
- As a [user type], I want [functionality] so that [benefit]

**Business Logic**:
- [Specific rules, calculations, or logic that must be implemented]
- [Edge cases and how they should be handled]
- [Data validation requirements]

### 3.2 Technical Requirements
**Architecture Considerations**:
- [Any specific architectural patterns to follow]
- [Integration points with existing systems]
- [Performance requirements]

**Data Requirements**:
- [New data models needed]
- [Changes to existing data models]
- [Data migration considerations]

**API Requirements**:
- [New endpoints needed]
- [Changes to existing endpoints]
- [Third-party integrations]

### 3.3 UI/UX Requirements
**Design Specifications**:
- [Specific UI components needed]
- [User flow descriptions]
- [Accessibility requirements]
- [Responsive design considerations]

**Visual Design**:
- [Color schemes, if specific]
- [Typography requirements]
- [Icon specifications]
- [Animation/transition requirements]

## 4. Implementation Guidance

### 4.1 Suggested Approach
**Phase 1**: [Initial implementation steps]
- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

**Phase 2**: [Follow-up implementation steps]
- [ ] Task 1
- [ ] Task 2

**Phase 3**: [Final implementation steps]
- [ ] Task 1
- [ ] Task 2

### 4.2 Files to Focus On
**Primary Files**:
- `path/to/file1.ext` - [Why this file is important]
- `path/to/file2.ext` - [Why this file is important]

**Secondary Files**:
- `path/to/file3.ext` - [What might need to change]
- `path/to/file4.ext` - [What might need to change]

**Related Documentation**:
- `docs/concepts/concept.md` - [Relevant concepts]
- `docs/architecture/architecture.md` - [Architectural decisions]

### 4.3 Dependencies & Prerequisites
**Required Before Starting**:
- [ ] Dependency 1
- [ ] Dependency 2

**Potential Blockers**:
- [Issue 1 and mitigation strategy]
- [Issue 2 and mitigation strategy]

## 5. Acceptance Criteria

### 5.1 Definition of Done
- [ ] All functional requirements implemented
- [ ] Code follows project conventions and patterns
- [ ] Unit tests written and passing
- [ ] Integration tests written and passing
- [ ] UI matches design specifications
- [ ] Performance requirements met
- [ ] Documentation updated
- [ ] Code reviewed and approved

### 5.2 Test Scenarios
**Happy Path**:
1. [Step-by-step test scenario]
2. [Expected result]

**Edge Cases**:
1. [Edge case scenario]
2. [Expected behavior]

**Error Cases**:
1. [Error scenario]
2. [Expected error handling]

### 5.3 Validation Methods
**Manual Testing**:
- [ ] Test case 1
- [ ] Test case 2

**Automated Testing**:
- [ ] Unit test coverage > 80%
- [ ] Integration tests pass
- [ ] End-to-end tests pass

## 6. Constraints & Considerations

### 6.1 Technical Constraints
- [Language/framework limitations]
- [Performance constraints]
- [Security requirements]
- [Third-party service limitations]

### 6.2 Business Constraints
- [Timeline constraints]
- [Budget constraints]
- [Resource constraints]

### 6.3 User Experience Constraints
- [Accessibility requirements]
- [Browser/device support requirements]
- [User workflow constraints]

## 7. Risk Assessment

### 7.1 Technical Risks
**Risk 1**: [Description]
- **Probability**: [High/Medium/Low]
- **Impact**: [High/Medium/Low]
- **Mitigation**: [Strategy to reduce risk]

**Risk 2**: [Description]
- **Probability**: [High/Medium/Low]
- **Impact**: [High/Medium/Low]
- **Mitigation**: [Strategy to reduce risk]

### 7.2 Business Risks
**Risk 1**: [Description]
- **Mitigation**: [Strategy to reduce risk]

## 8. Success Metrics
**Quantitative Metrics**:
- [Metric 1]: [Target value]
- [Metric 2]: [Target value]

**Qualitative Metrics**:
- [User satisfaction indicator]
- [Usability improvement]

## 9. Future Considerations
**Potential Enhancements**:
- [Enhancement 1]: [Why it might be valuable later]
- [Enhancement 2]: [Why it might be valuable later]

**Scalability Considerations**:
- [How this change affects future scaling]
- [Areas that might need attention as usage grows]

## 10. Claude Implementation Notes
**Optimization Tips for AI Development**:
- [ ] Provide specific file paths and line numbers when referencing existing code
- [ ] Include relevant code snippets in the prompt for context
- [ ] Specify exact error messages or behaviors to look for
- [ ] Mention specific testing commands to run
- [ ] Include examples of expected input/output
- [ ] Reference existing patterns to follow in the codebase
- [ ] Specify whether to create new files or modify existing ones
- [ ] Include any specific libraries or dependencies to use/avoid

**Context Preservation**:
- [ ] Reference related PRPs or previous implementations
- [ ] Include links to relevant documentation
- [ ] Mention any architectural decisions that affect this change
- [ ] Note any code conventions specific to this area of the codebase

---

## Template Usage Instructions

1. **Copy this template** for each new feature or change request
2. **Fill out all relevant sections** - skip sections that don't apply but note why
3. **Be as specific as possible** - vague requirements lead to inefficient implementations
4. **Include examples** wherever possible
5. **Reference existing code** to maintain consistency
6. **Update the PRP** as requirements evolve during implementation
7. **Use this as a checklist** during development to ensure nothing is missed

## Example PRP References
- `docs/prps/wallet-list-implementation.md` - Example of a complex feature PRP
- `docs/prps/transaction-validation-update.md` - Example of a technical improvement PRP
- `docs/prps/ui-enhancement-simple.md` - Example of a simple UI change PRP