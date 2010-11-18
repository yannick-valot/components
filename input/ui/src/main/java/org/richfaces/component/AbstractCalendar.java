/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.EventName;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.annotations.JsfRenderer;
import org.richfaces.cdk.annotations.Tag;
import org.richfaces.context.ExtendedVisitContext;
import org.richfaces.context.ExtendedVisitContextMode;
import org.richfaces.event.CurrentDateChangeEvent;
import org.richfaces.event.CurrentDateChangeListener;
import org.richfaces.log.Logger;
import org.richfaces.log.RichfacesLogger;
import org.richfaces.model.CalendarDataModel;
import org.richfaces.model.CalendarDataModelItem;
import org.richfaces.renderkit.MetaComponentRenderer;
import org.richfaces.utils.CalendarHelper;


/**
 * @author amarkhel
 * 
 */

@JsfComponent(type = AbstractCalendar.COMPONENT_TYPE, family = AbstractCalendar.COMPONENT_FAMILY, generate = "org.richfaces.component.UICalendar", renderer = @JsfRenderer(type = "org.richfaces.CalendarRenderer"), tag = @Tag(name = "calendar"))
public abstract class AbstractCalendar extends UIInput implements MetaComponentResolver, MetaComponentEncoder {

    public static final String DAYSDATA_META_COMPONENT_ID = "daysData";
    
    public static final String COMPONENT_TYPE = "org.richfaces.Calendar";

    public static final String COMPONENT_FAMILY = "org.richfaces.Calendar";

    public static final String SUB_TIME_PATTERN = "\\s*[hHkKma]+[\\W&&\\S]+[hHkKma]+\\s*";

    public static final String TIME_PATTERN = "HH:mm";

    public static final String DEFAULT_DATE_PATTERN = "MMM d, yyyy";

    Logger log = RichfacesLogger.COMPONENTS.getLogger();

    protected enum PropertyKeys {
        locale
    };

    public enum Modes {
        CLIENT,
        AJAX
    }
    
    
    @Attribute(defaultValue = "MMM d, yyyy")
    public abstract String getDatePattern();

    @Attribute(defaultValue = "TimeZone.getDefault()")
    public abstract TimeZone getTimeZone();

    @Attribute(defaultValue = "Integer.MIN_VALUE")
    public abstract int getFirstWeekDay();

    @Attribute(defaultValue = "Integer.MIN_VALUE")
    public abstract int getMinDaysInFirstWeek();

    @Attribute(defaultValue = "select")
    public abstract String getTodayControlMode();

    @Attribute(defaultValue = "true")
    public abstract boolean isShowWeekDaysBar();

    @Attribute(defaultValue = "true")
    public abstract boolean isShowWeeksBar();

    @Attribute(defaultValue = "true")
    public abstract boolean isShowFooter();

    @Attribute(defaultValue = "true")
    public abstract boolean isShowHeader();

    @Attribute(defaultValue = "true")
    public abstract boolean isShowInput();

    @Attribute(defaultValue = "true")
    public abstract boolean isPopup();

    @Attribute(defaultValue = "true")
    public abstract String getHidePopupOnScroll();

    @Attribute(defaultValue = "false")
    public abstract boolean isDisabled();

    @Attribute(defaultValue = "false")
    public abstract boolean isEnableManualInput();

    @Attribute(defaultValue = "false")
    public abstract boolean isDayEnabled();

    @Attribute(defaultValue = "false")
    public abstract boolean isShowApplyButton();

    @Attribute(defaultValue = "false")
    public abstract boolean isResetTimeOnDateSelect();

    @Attribute(defaultValue = "AA")
    public abstract String getJointPoint();

    @Attribute(defaultValue = "AA")
    public abstract String getDirection();

    @Attribute(defaultValue = "inactive")
    public abstract String getBoundaryDatesMode();

    @Attribute(defaultValue = "0")
    public abstract String getHorizontalOffset();

    @Attribute(defaultValue = "0")
    public abstract String getVerticalOffset();

    @Attribute(defaultValue = "3")
    public abstract int getZindex();
    
    @Attribute(defaultValue = "client")
    public abstract String getMode(); 

    @Attribute
    public abstract String getStyle();

    @Attribute
    public abstract Object getMonthLabels();

    @Attribute
    public abstract Object getMonthLabelsShort();

    @Attribute
    public abstract Object getWeekDayLabelsShort();

    @Attribute
    public abstract Object getWeekDayLabels();

    @Attribute
    public abstract String getDayStyleClass();

    @Attribute
    public abstract String getTabindex();

    @Attribute
    public abstract String getInputStyle();

    @Attribute
    public abstract String getButtonClass();

    @Attribute
    public abstract String getInputClass();

    @Attribute
    public abstract String getButtonLabel();

    @Attribute
    public abstract String getInputSize();

    @Attribute
    public abstract Object getCurrentDate();

    @Attribute
    public abstract void setCurrentDate(Object date);

    @Attribute
    public abstract String getButtonIcon();

    @Attribute
    public abstract String getButtonIconDisabled();

    @Attribute
    public abstract Object getDefaultTime();
    
    @Attribute(defaultValue = "getDefaultPreloadBegin(getCurrentDateOrDefault())")
    public abstract Object getPreloadDateRangeBegin();
    
    public abstract void setPreloadDateRangeBegin(Object date);
    
    @Attribute(defaultValue = "getDefaultPreloadEnd(getCurrentDateOrDefault())")
    public abstract Object getPreloadDateRangeEnd();
    
    public abstract void setPreloadDateRangeEnd(Object date);
    
    @Attribute
    public abstract CalendarDataModel getDataModel();

    @Attribute(events = @EventName("inputclick"))
    public abstract String getOninputclick();

    @Attribute(events = @EventName("inputdblclick"))
    public abstract String getOninputdblclick();

    @Attribute(events = @EventName("inputchange"))
    public abstract String getOninputchange();

    @Attribute(events = @EventName("inputselect"))
    public abstract String getOninputselect();

    @Attribute(events = @EventName("inputmousedown"))
    public abstract String getOninputmousedown();

    @Attribute(events = @EventName("inputmousemove"))
    public abstract String getOninputmousemove();

    @Attribute(events = @EventName("inputmouseout"))
    public abstract String getOninputmouseout();

    @Attribute(events = @EventName("inputmouseover"))
    public abstract String getOninputmouseover();

    @Attribute(events = @EventName("inputmouseup"))
    public abstract String getOninputmouseup();

    @Attribute(events = @EventName("inputkeydown"))
    public abstract String getOninputkeydown();

    @Attribute(events = @EventName("inputkeypress"))
    public abstract String getOninputkeypress();

    @Attribute(events = @EventName("inputkeyup"))
    public abstract String getOninputkeyup();

    @Attribute(events = @EventName("inputfocus"))
    public abstract String getOninputfocus();

    @Attribute(events = @EventName("inputblur"))
    public abstract String getOninputblur();

    @Attribute
    public Object getLocale() {
        Object locale = getStateHelper().eval(PropertyKeys.locale);
        if (locale == null) {
            FacesContext facesContext = getFacesContext();
            UIViewRoot viewRoot = facesContext.getViewRoot();
            if (viewRoot != null) {
                locale = viewRoot.getLocale();
            }
        }
        return locale != null ? locale : Locale.US;
    }

    public void setLocale(Object locale) {
        getStateHelper().put(PropertyKeys.locale, locale);
    }

    public void updateCurrentDate(FacesContext facesContext, Object currentDate) {
        if (facesContext == null) {
            throw new NullPointerException();
        }
        // RF-1073
        try {
            ValueExpression ve = getValueExpression("currentDate");
            if (ve != null) {
                ELContext elContext = facesContext.getELContext();
                if (ve.getType(elContext).equals(String.class)) {
                    DateTimeConverter convert = new DateTimeConverter();
                    convert.setLocale(CalendarHelper.getAsLocale(facesContext, this, getLocale()));
                    convert.setPattern(getDatePattern());
                    ve.setValue(facesContext.getELContext(), convert.getAsString(facesContext, this, currentDate));
                    return;
                } else if (ve.getType(elContext).equals(Calendar.class)) {
                    Calendar c = Calendar.getInstance();
                    c.setTime((Date) currentDate);
                    ve.setValue(elContext, c);
                    return;
                } else {
                    ve.setValue(elContext, currentDate);
                    return;
                }
            } else {
                setCurrentDate(currentDate);
            }

        } catch (Exception e) {
            setValid(false);
            if (log.isDebugEnabled()) {
                log.debug(" updateCurrentDate method throws exception: " + e.toString(), e);
            }

            String messageString = e.toString();
            FacesMessage message = new FacesMessage(messageString);
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            facesContext.addMessage(getClientId(facesContext), message);
        }
    }

    public void addCurrentDateChangeListener(CurrentDateChangeListener listener) {
        addFacesListener(listener);
    }

    public void removeToggleListener(CurrentDateChangeListener listener) {
        removeFacesListener(listener);
    }

    public CurrentDateChangeListener[] getCurrentDateChangeListeners() {
        return (CurrentDateChangeListener[]) getFacesListeners(CurrentDateChangeListener.class);
    }
    
    protected Date getDefaultPreloadBegin(Date date) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Calendar calendar = Calendar.getInstance(getTimeZone(),
            CalendarHelper.getAsLocale(facesContext, this, getLocale()));
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        return calendar.getTime();
    }

    protected Date getDefaultPreloadEnd(Date date) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Calendar calendar = Calendar.getInstance(getTimeZone(),
            CalendarHelper.getAsLocale(facesContext, this, getLocale()));
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        /*
         * //force recalculation calendar.getTimeInMillis();
         * calendar.set(Calendar.DAY_OF_WEEK, getLastDayOfWeek(calendar));
         */
        return calendar.getTime();
    }
    
    public Date getCurrentDateOrDefault() {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        Date date = CalendarHelper.getAsDate(facesContext, this, getCurrentDate());

        if (date != null) {
            return date;
        } else {
            Date value = CalendarHelper.getAsDate(facesContext, this, this.getValue());
            if (value != null) {
                return value;
            } else {
                return java.util.Calendar.getInstance(getTimeZone()).getTime();
            }
        }
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof CurrentDateChangeEvent) {
            FacesContext facesContext = getFacesContext();
            CurrentDateChangeEvent currentDateChangeEvent = (CurrentDateChangeEvent) event;
            String currentDateString = currentDateChangeEvent.getCurrentDateString();

            try {
                // we should use datePattern attribute-based converter only for
                // selectedDate
                // current date string always has predefined format: m/y
                Date currentDate = CalendarHelper.getAsDate(facesContext, this, getCurrentDate());
                Date submittedCurrentDate = CalendarHelper.convertCurrentDate(currentDateString);
                currentDateChangeEvent.setCurrentDate(submittedCurrentDate);

                if (!submittedCurrentDate.equals(currentDate)) {
                    updateCurrentDate(facesContext, submittedCurrentDate);
                }

            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(" currentDate convertion fails with following exception: " + e.toString(), e);
                }
                setValid(false);
                String messageString = e.toString();
                FacesMessage message = new FacesMessage(messageString);
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                facesContext.addMessage(getClientId(facesContext), message);
                facesContext.renderResponse();
            }
        } else {
            super.broadcast(event);
        }
    }

    @Override
    public void queueEvent(FacesEvent event) {
        super.queueEvent(event);
        if (event instanceof CurrentDateChangeEvent) {
            event.setPhaseId(PhaseId.PROCESS_VALIDATIONS);
        }
    }
    
    public String resolveClientId(FacesContext facesContext, UIComponent contextComponent, String metaComponentId) {
        if (DAYSDATA_META_COMPONENT_ID.equals(metaComponentId)) {
            return getClientId(facesContext) + MetaComponentResolver.META_COMPONENT_SEPARATOR_CHAR + metaComponentId;
        }
        return null;
    }

    public String substituteUnresolvedClientId(FacesContext facesContext, UIComponent contextComponent,
        String metaComponentId) {
        return null;
    }
    
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        if (context instanceof ExtendedVisitContext) {
            ExtendedVisitContext extendedVisitContext = (ExtendedVisitContext) context;
            if (extendedVisitContext.getVisitMode() == ExtendedVisitContextMode.RENDER) {

                VisitResult result = extendedVisitContext.invokeMetaComponentVisitCallback(this, callback,
                    DAYSDATA_META_COMPONENT_ID);
                if (result == VisitResult.COMPLETE) {
                    return true;
                } else if (result == VisitResult.REJECT) {
                    return false;
                }
            }
        }

        return super.visitTree(context, callback);
    }

    public void encodeMetaComponent(FacesContext context, String metaComponentId) throws IOException {
        ((MetaComponentRenderer) getRenderer(context)).encodeMetaComponent(context, this, metaComponentId);
    }    
    
    public Object getPreload() {
        Date[] preloadDateRange = getPreloadDateRange();
        if (preloadDateRange != null && preloadDateRange.length != 0) {
            CalendarDataModel calendarDataModel = (CalendarDataModel) getDataModel();
            if (calendarDataModel != null) {
                CalendarDataModelItem[] calendarDataModelItems = calendarDataModel
                        .getData(preloadDateRange);

                HashMap<String, Object> args = new HashMap<String, Object>();
                
                args.put("startDate", formatStartDate(preloadDateRange[0]));
                args.put("days", calendarDataModelItems);
                return args;
            }
        }
        return null;
    }
    
    public static Object formatStartDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        HashMap <String, Object> hashDate = new HashMap<String,Object>();
        hashDate.put("month", calendar.get(Calendar.MONTH));
        hashDate.put("year", calendar.get(Calendar.YEAR));
        return hashDate;
    }
    
    public Date[] getPreloadDateRange() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        Date dateRangeBegin = null;
        Date dateRangeEnd = null;
        
        if (Modes.AJAX.toString().equalsIgnoreCase(getMode())) {
            dateRangeBegin = CalendarHelper.getAsDate(facesContext, this, 
                getDefaultPreloadBegin((Date) getCurrentDate()));
            dateRangeEnd = CalendarHelper.getAsDate(facesContext, this, 
                getDefaultPreloadEnd((Date) getCurrentDate()));                
        } else {
            dateRangeBegin = CalendarHelper.getAsDate(facesContext, this, getPreloadDateRangeBegin());
            dateRangeEnd = CalendarHelper.getAsDate(facesContext, this, getPreloadDateRangeEnd());            
        }

        if (dateRangeBegin == null && dateRangeEnd == null) {
            return null;
        } else {
            if (dateRangeBegin.after(dateRangeEnd)) {
                // XXX add message
                FacesMessage message = new FacesMessage(
                        "preloadDateRangeBegin is greater than preloadDateRangeEnd");
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                facesContext.addMessage(getClientId(facesContext), message);
                throw new IllegalArgumentException();
            }

            List<Date> dates = new ArrayList<Date>();

            Calendar calendar = Calendar.getInstance(this.getTimeZone(), CalendarHelper.getAsLocale(facesContext, this, this.getLocale()));
            Calendar calendar2 = (Calendar) calendar.clone();
            calendar.setTime(dateRangeBegin);
            calendar2.setTime(dateRangeEnd);

            do {
                dates.add(calendar.getTime());
                calendar.add(Calendar.DATE, 1);
            } while (!calendar.after(calendar2));

            return (Date[]) dates.toArray(new Date[dates.size()]);
        }
    }
}