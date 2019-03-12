# Code Smells
  1. Zeros should not be a possible denominator
  2. **Wildcard imports should not be used**
  3. **Variables should not be self-assigned**
  4. Values should not be uselessly incremented (assignment in a post-increment to self or returning a post-inc only)
  5. Utility classes should not have public constructors (any static class should have no public constructor)
  6. **Unary prefix operators should not be repeated (!, ~, -, and +)**
  7. **Thread.run() should not be called directly**
  8. The default unnamed package should not be used
  9. The Object.finalize() method should not be overridden
  10. The Object.finalize() method should not be called
  11. **Ternary operators should not be nested**
