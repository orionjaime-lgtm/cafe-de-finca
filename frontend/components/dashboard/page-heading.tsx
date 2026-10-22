type PageHeadingProps = {
  eyebrow: string
  title: string
  description: string
}

export function PageHeading({ eyebrow, title, description }: PageHeadingProps) {
  return (
    <div>
      <p className="text-xs font-semibold tracking-[0.14em] text-accent">{eyebrow}</p>
      <h1 className="mt-2 text-3xl font-bold tracking-tight text-balance text-foreground">{title}</h1>
      <p className="mt-2 max-w-2xl text-sm leading-relaxed text-pretty text-muted-foreground">
        {description}
      </p>
    </div>
  )
}
