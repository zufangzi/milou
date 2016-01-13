package com.dingding.milou.dbunit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.dataset.IDataSet;

import com.github.springtestdbunit.dataset.DataSetModifier;

public class DataSetModifiers implements DataSetModifier {

    private final List<DataSetModifier> modifiers = new ArrayList<DataSetModifier>();

    public IDataSet modify(IDataSet dataSet) {
        for (DataSetModifier modifier : this.modifiers) {
            dataSet = modifier.modify(dataSet);
        }
        return dataSet;
    }

    public void add(Object testInstance, Class<? extends DataSetModifier> modifierClass) {
        try {
            Class<?> enclosingClass = modifierClass.getEnclosingClass();
            if ((enclosingClass == null) || Modifier.isStatic(modifierClass.getModifiers())) {
                add(modifierClass.getDeclaredConstructor());
            } else {
                add(modifierClass.getDeclaredConstructor(enclosingClass), testInstance);
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void add(Constructor<? extends DataSetModifier> constructor, Object...args) throws Exception {
        constructor.setAccessible(true);
        this.modifiers.add(constructor.newInstance(args));
    }

}
