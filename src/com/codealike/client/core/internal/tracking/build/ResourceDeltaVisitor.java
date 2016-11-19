/*package com.codealike.client.core.internal.tracking.build;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
*/
/**
 * Tracks projects affected by changes over the workspace.
 * @author jesica.fera
 *
 */
/*public class ResourceDeltaVisitor implements IResourceDeltaVisitor{

	private List<IProject> projects;
	private int trackOnly;
	private int changedFlags;
	
	public ResourceDeltaVisitor() {
		this.projects = new LinkedList<IProject>();
		this.trackOnly = IResourceDelta.ALL_WITH_PHANTOMS;
		this.changedFlags = IResourceDelta.CONTENT | IResourceDelta.MOVED_FROM | IResourceDelta.MOVED_TO | IResourceDelta.COPIED_FROM 
				| IResourceDelta.OPEN | IResourceDelta.TYPE | IResourceDelta.SYNC | IResourceDelta.MARKERS | IResourceDelta.REPLACED 
				| IResourceDelta.DESCRIPTION | IResourceDelta.ENCODING | IResourceDelta.LOCAL_CHANGED | IResourceDelta.DERIVED_CHANGED;
	}
	
	public ResourceDeltaVisitor(int trackOnly) {
		this();
		this.trackOnly = trackOnly;
	}
	*/
	/**
	 * 
	 * @param trackOnly specific type of deltas to track (bitmask). Default value: IResourceDelta.ALL_WITH_PHANTOMS. 
	 * @param changedFlags specific changed flags to track.
	 * @see #IResourceDelta
	 */
	/*public ResourceDeltaVisitor(int trackOnly, int changedFlags) {
		this();
		this.trackOnly = trackOnly;
		this.changedFlags = changedFlags;
	}
	
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		
		if ((delta.getKind() & trackOnly) != 0) {
			IResource res = delta.getResource();
			if (res == null || res.getType() != IResource.PROJECT) {
				return res.getType() == IResource.ROOT;
			}
			
			if (delta.getKind() == IResourceDelta.CHANGED && delta.getFlags() != IResourceDelta.NO_CHANGE 
					&& (delta.getFlags() & this.changedFlags) == 0) {
				return true;
			}
			
			IProject project = res.getProject();
			if ( project != null) {
				if (!projects.contains(project)) {
					projects.add(project);
				}
				return false;
			}
		}
		return true;

	}
	
	public List<IProject> getAffectedProjects()
	{
		return this.projects;
	}

}
*/