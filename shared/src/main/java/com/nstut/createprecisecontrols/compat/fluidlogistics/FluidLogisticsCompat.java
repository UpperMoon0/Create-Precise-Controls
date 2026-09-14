package com.nstut.createprecisecontrols.compat.fluidlogistics;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Optional bridge to Create: FluidLogistics' public package-resource API.
 *
 * <p>The addon is intentionally not a compile/runtime dependency. Its 1.2.6 and 1.2.9
 * branches expose the same PackageResources/PackageResourceDisplay contract used here,
 * so reflection keeps Precise Controls client-only and independently installable while
 * still deriving limits and units from FluidLogistics instead of duplicating them.</p>
 */
public final class FluidLogisticsCompat {
    private static final String PACKAGE_RESOURCES = "com.yision.fluidlogistics.api.packager.PackageResources";
    private static final String PACKAGE_RESOURCE_DISPLAY =
            "com.yision.fluidlogistics.api.packager.PackageResourceDisplay";
    private static final String FACTORY_PANEL_POLICY =
            "com.yision.fluidlogistics.api.packager.PackageResourceDisplay$FactoryPanelRestockPolicy";

    private static volatile Reflection reflection;
    private static volatile boolean lookupAttempted;

    private FluidLogisticsCompat() {}

    public record ResourceAmountSpec(String baseUnit, int maxRequestPerBatch) {
        public ResourceAmountSpec {
            if (baseUnit == null || baseUnit.isBlank()) baseUnit = "units";
            maxRequestPerBatch = Math.max(1, maxRequestPerBatch);
        }
    }

    /** Returns FluidLogistics' exact resource unit and the same batch cap its scroll hook uses. */
    public static Optional<ResourceAmountSpec> recipeAmountSpec(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        Reflection api = reflection();
        if (api == null) return Optional.empty();
        try {
            Object optional = api.displayOf.invoke(null, stack);
            if (!(optional instanceof Optional<?> result) || result.isEmpty()) return Optional.empty();

            Object display = result.get();
            String unit = String.valueOf(api.baseUnit.invoke(display)).trim();
            Object policy = api.factoryPanelRestockPolicy.invoke(display, stack);
            int maximum = ((Number) api.maxRequestPerBatch.invoke(policy)).intValue();
            return Optional.of(new ResourceAmountSpec(unit, maximum));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public static boolean isResource(ItemStack stack) {
        return recipeAmountSpec(stack).isPresent();
    }

    private static Reflection reflection() {
        if (lookupAttempted) return reflection;
        synchronized (FluidLogisticsCompat.class) {
            if (lookupAttempted) return reflection;
            lookupAttempted = true;
            try {
                ClassLoader loader = FluidLogisticsCompat.class.getClassLoader();
                Class<?> resourcesClass = Class.forName(PACKAGE_RESOURCES, false, loader);
                Class<?> displayClass = Class.forName(PACKAGE_RESOURCE_DISPLAY, false, loader);
                Class<?> policyClass = Class.forName(FACTORY_PANEL_POLICY, false, loader);
                reflection = new Reflection(
                        resourcesClass.getMethod("displayOf", ItemStack.class),
                        displayClass.getMethod("baseUnit"),
                        displayClass.getMethod("factoryPanelRestockPolicy", ItemStack.class),
                        policyClass.getMethod("maxRequestPerBatch"));
            } catch (ReflectiveOperationException | LinkageError ignored) {
                reflection = null;
            }
            return reflection;
        }
    }

    private record Reflection(Method displayOf, Method baseUnit,
                              Method factoryPanelRestockPolicy, Method maxRequestPerBatch) {}
}
