// Zustand mock import (see '/home/aasaad/projects/keip-canvas/ui/__mocks__/zustand.ts')
// @ts-expect-error the reset function does not exist as a zustand export.
// It is added to the mock as a convenience for setup and cleanup of the store in the tests
import { resetStore } from "zustand"

export const resetMockStore = resetStore as (s: object) => void
