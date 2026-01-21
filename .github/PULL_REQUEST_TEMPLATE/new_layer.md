## New Benchmark Layer/Category

Thank you for proposing a new category of benchmark applications!

### New Layer Details

- **Layer/Category Name**: 
- **Directory Path**: `benchmark/<new-category>/`
- **Purpose**: 

### Rationale

Explain why this new category is needed and what gap it fills in the existing benchmark suite:

### Scope and Patterns

Describe the types of applications that would belong in this category:

### Example Applications

List 2-3 example applications you plan to include in this category (or are including in this PR):

1. **Application Name**: 
   - **Description**: 
   - **Key Features**: 

2. **Application Name**: 
   - **Description**: 
   - **Key Features**: 

3. **Application Name**: 
   - **Description**: 
   - **Key Features**: 

## Relationship to Existing Categories

Explain how this category differs from existing ones:

- **business_domain/** - 
- **dependency_injection/** - 
- **infrastructure/** - 
- **integration/** - 
- **persistence/** - 
- **presentation/** - 
- **security/** - 
- **whole_applications/** - 

## Category Structure

Proposed directory structure:
```
benchmark/<new-category>/
├── README.md                    # Category overview
├── <app-1>/
│   ├── README.md
│   ├── jakarta/
│   ├── quarkus/
│   └── spring/
├── <app-2>/
│   ├── README.md
│   ├── jakarta/
│   ├── quarkus/
│   └── spring/
└── ...
```

## Documentation

- [ ] Created `benchmark/<new-category>/README.md` describing the category
- [ ] Documented what types of applications belong here
- [ ] Provided examples of patterns this category covers
- [ ] Explained differences from existing categories

### Category README Content

The category README should include:
- [ ] Purpose and scope of the category
- [ ] Types of patterns/technologies covered
- [ ] List of applications in the category
- [ ] Common testing approaches for this category

## Initial Applications

If including applications in this PR:

### Applications Included

- [ ] At least 2 example applications
- [ ] Each application has all three framework implementations
- [ ] All applications follow standard patterns (Dockerfile, justfile, smoke tests)

### Testing Completed

For each included application and framework:
- [ ] `just build` succeeds
- [ ] `just up` starts successfully
- [ ] `just test` passes
- [ ] `just down` cleans up

## Website/Documentation Updates

- [ ] Site documentation needs updating (in `site/docs/`)
- [ ] Category needs to be added to benchmark overview
- [ ] Navigation/table of contents needs updating
- [ ] Will be handled in follow-up PR
- [ ] N/A

## Related Issues

Closes #(issue number)
Related to #(issue number)

## Community Feedback

Have you discussed this new category with maintainers?
- [ ] Yes, discussed in issue #(number)
- [ ] Yes, discussed in community forum/chat
- [ ] No, proposing for discussion in this PR

## Additional Context

Any other information about this new category proposal:

---

**Reviewers**: Please consider:
- [ ] Is this category distinct enough to warrant separation?
- [ ] Does it overlap too much with existing categories?
- [ ] Are the example applications appropriate?
- [ ] Is the category well-defined and scoped?
- [ ] Will this be useful for benchmarking migration patterns?
- [ ] Is documentation clear and complete?
