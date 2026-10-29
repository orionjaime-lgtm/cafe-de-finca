'use client'

import { createContext, useContext, useEffect, useState } from 'react'

export type ThemePreference = 'light' | 'dark'
export type FontSizePreference = 'small' | 'medium' | 'large'

type PreferencesContextType = {
  theme: ThemePreference
  fontSize: FontSizePreference
  setTheme: (theme: ThemePreference) => void
  setFontSize: (fontSize: FontSizePreference) => void
  resetPreferences: () => void
}

const PreferencesContext = createContext<PreferencesContextType | null>(null)

export function PreferencesProvider({ children }: { children: React.ReactNode }) {
  const [theme, setThemeState] = useState<ThemePreference>('light')
  const [fontSize, setFontSizeState] = useState<FontSizePreference>('medium')
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    const savedTheme = localStorage.getItem('cafe-theme')
    const savedFontSize = localStorage.getItem('cafe-font-size')
    if (savedTheme === 'dark' || savedTheme === 'light') setThemeState(savedTheme)
    if (savedFontSize === 'small' || savedFontSize === 'medium' || savedFontSize === 'large') setFontSizeState(savedFontSize)
    setLoaded(true)
  }, [])

  useEffect(() => {
    if (!loaded) return
    const root = document.documentElement
    root.classList.toggle('dark', theme === 'dark')
    root.dataset.theme = theme
    root.dataset.fontSize = fontSize
    localStorage.setItem('cafe-theme', theme)
    localStorage.setItem('cafe-font-size', fontSize)
  }, [theme, fontSize, loaded])

  function setTheme(nextTheme: ThemePreference) {
    setThemeState(nextTheme)
  }

  function setFontSize(nextFontSize: FontSizePreference) {
    setFontSizeState(nextFontSize)
  }

  function resetPreferences() {
    setThemeState('light')
    setFontSizeState('medium')
  }

  return (
    <PreferencesContext.Provider value={{ theme, fontSize, setTheme, setFontSize, resetPreferences }}>
      {children}
    </PreferencesContext.Provider>
  )
}

export function usePreferences() {
  const context = useContext(PreferencesContext)
  if (!context) throw new Error('usePreferences debe usarse dentro de PreferencesProvider')
  return context
}
